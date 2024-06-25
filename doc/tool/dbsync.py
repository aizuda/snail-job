# encoding=utf8
"""SnailJob

Author: dhb52

pip install simple-ddl-parser
"""

import argparse
import pathlib
import re
import time
from abc import ABC, abstractmethod
from typing import Dict, Generator, Optional, Tuple, Union

from simple_ddl_parser import DDLParser

PREAMBLE = """/*
 SnailJob Database Transfer Tool
 Source Server Type    : MySQL
 Target Server Type    : {db_type}
 Date: {date}
*/

"""


def load_and_clean(sql_file: str) -> str:
    """加载源 SQL 文件，并清理内容方便下一步 ddl 解析

    Args:
        sql_file (str): sql文件路径

    Returns:
        str: 清理后的sql文件内容
    """
    REPLACE_PAIR_LIST = (
        (")\nVALUES ", ") VALUES "),
        (" CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ", " "),
        (" KEY `", " INDEX `"),
        ("UNIQUE INDEX", "UNIQUE KEY"),
        ("b'0'", "'0'"),
        ("b'1'", "'1'"),
    )

    content = open(sql_file, encoding="utf-8").read()
    for replace_pair in REPLACE_PAIR_LIST:
        content = content.replace(*replace_pair)
    content = re.sub(r"ENGINE.*COMMENT", "COMMENT", content)
    content = re.sub(r"ENGINE.*;", ";", content)
    return content


class Convertor(ABC):
    def __init__(self, src: str, db_type) -> None:
        self.src = src
        self.db_type = db_type
        self.content = load_and_clean(self.src)
        self.table_script_list = re.findall(r"CREATE TABLE [^;]*;", self.content)

    @abstractmethod
    def translate_type(self, type: str, size: Optional[Union[int, Tuple[int]]]) -> str:
        """字段类型转换

        Args:
            type (str): 字段类型
            size (Optional[Union[int, Tuple[int]]]): 字段长度描述, 如varchar(255), decimal(10,2)

        Returns:
            str: 类型定义
        """
        pass

    @abstractmethod
    def gen_create(self, table_ddl: Dict) -> str:
        """生成 create 脚本

        Args:
            table_ddl (Dict): 表DDL

        Returns:
            str:  生成脚本
        """
        pass

    @abstractmethod
    def gen_pk(self, table_name: str) -> str:
        """生成主键定义

        Args:
            table_name (str): 表名

        Returns:
            str: 生成脚本
        """
        pass

    @abstractmethod
    def gen_index(self, ddl: Dict) -> str:
        """生成索引定义

        Args:
            table_ddl (Dict): 表DDL

        Returns:
            str: 生成脚本
        """
        pass

    @abstractmethod
    def gen_comment(self, table_ddl: Dict) -> str:
        """生成字段/表注释

        Args:
            table_ddl (Dict): 表DDL

        Returns:
            str: 生成脚本
        """
        pass

    @abstractmethod
    def gen_uk(self, table_ddl: Dict) -> str:
        """生成

        Args:
            table_ddl (Dict): 表DDL

        Returns:
            str: 生成脚本
        """

    @abstractmethod
    def gen_insert(self, table_name: str) -> str:
        """生成 insert 语句块

        Args:
            table_name (str): 表名

        Returns:
            str: 生成脚本
        """
        pass

    @staticmethod
    def inserts(table_name: str, script_content: str) -> Generator:
        PREFIX = f"INSERT INTO `{table_name}`"

        # 收集 `table_name` 对应的 insert 语句
        for line in script_content.split("\n"):
            if line.startswith(PREFIX):
                head, tail = line.replace(PREFIX, "").split(" VALUES ", maxsplit=1)
                head = head.strip().replace("`", "").lower()
                tail = tail.strip().replace(r"\"", '"')
                # tail = tail.replace("b'0'", "'0'").replace("b'1'", "'1'")
                yield f"INSERT INTO {table_name.lower()} {head} VALUES {tail}"

    @staticmethod
    def index(ddl: Dict) -> Generator:
        """生成索引定义

        Args:
            ddl (Dict): 表DDL

        Yields:
            Generator[str]: create index 语句
        """

        def generate_columns(columns):
            keys = [
                f"{col['name'].lower()}{' ' + col['order'].lower() if col['order'] != 'ASC' else ''}"
                for col in columns[0]
            ]
            return ", ".join(keys)

        for no, index in enumerate(ddl["index"], 1):
            columns = generate_columns(index["columns"])
            table_name = ddl["table_name"].lower()
            yield f"CREATE INDEX idx_{table_name}_{no:02d} ON {table_name} ({columns})"

    @staticmethod
    def unique_index(ddl: Dict) -> Generator:
        if "constraints" in ddl and "uniques" in ddl["constraints"]:
            uk_list = ddl["constraints"]["uniques"]
            for uk in uk_list:
                table_name = ddl["table_name"]
                uk_name = uk["constraint_name"]
                uk_columns = uk["columns"]
                yield table_name, uk_name, uk_columns

    @staticmethod
    def filed_comments(table_sql: str) -> Generator:
        for line in table_sql.split("\n"):
            match = re.match(r"^`([^`]+)`.* COMMENT '([^']+)'", line.strip())
            if match:
                field = match.group(1)
                comment_string = match.group(2).replace("\\n", "\n")
                yield field, comment_string

    def table_comment(self, table_sql: str) -> str:
        match = re.search(r"COMMENT \='([^']+)';", table_sql)
        return match.group(1) if match else None

    def print(self):
        """打印转换后的sql脚本到终端"""
        print(
            PREAMBLE.format(
                db_type=self.db_type,
                date=time.strftime("%Y-%m-%d %H:%M:%S"),
            )
        )

        error_scripts = []
        for table_sql in self.table_script_list:
            ddl = DDLParser(table_sql.replace("`", "")).run()

            # 如果parse失败, 需要跟进
            if len(ddl) == 0:
                error_scripts.append(table_sql)
                continue

            table_ddl = ddl[0]
            table_name = table_ddl["table_name"]

            # 解析注释
            for column in table_ddl["columns"]:
                column["comment"] = bytes(column["comment"], "utf-8").decode(
                    "unicode_escape"
                )[1:-1]
            table_ddl["comment"] = bytes(table_ddl["comment"], "utf-8").decode(
                "unicode_escape"
            )[1:-1]

            # 为每个表生成个6个基本部分
            create = self.gen_create(table_ddl)
            pk = self.gen_pk(table_name)
            uk = self.gen_uk(table_ddl)
            index = self.gen_index(table_ddl)
            comment = self.gen_comment(table_ddl)
            inserts = self.gen_insert(table_name)

            # 组合当前表的DDL脚本
            script = f"""{create}

{pk}

{uk}

{index}

{comment}

{inserts}
"""

            # 清理
            script = re.sub("\n{3,}", "\n\n", script).strip() + "\n"

            print(script)

        # 将parse失败的脚本打印出来
        if error_scripts:
            for script in error_scripts:
                print(script)


class PostgreSQLConvertor(Convertor):
    def __init__(self, src):
        super().__init__(src, "PostgreSQL")

    def translate_type(self, type: str, size: Optional[Union[int, Tuple[int]]]):
        """类型转换"""

        type = type.lower()

        if type == "varchar":
            return f"varchar({size})"
        if type in ("int", "int unsigned"):
            return "int"
        if type in ("bigint", "bigint unsigned"):
            return "bigint"
        if type == "datetime":
            return "timestamp"
        if type == "timestamp":
            return f"timestamp({size})"
        if type == "bit":
            return "bool"
        if type in ("tinyint", "smallint"):
            return "smallint"
        if type in ("text", "longtext"):
            return "text"
        if type in ("blob", "mediumblob"):
            return "bytea"
        if type == "decimal":
            return (
                f"numeric({','.join(str(s) for s in size)})" if len(size) else "numeric"
            )

    def gen_create(self, ddl: Dict) -> str:
        """生成 create"""

        def _generate_column(col):
            name = col["name"].lower()
            if name == "id":
                return "id bigserial PRIMARY KEY"

            type = col["type"].lower()
            full_type = self.translate_type(type, col["size"])
            nullable = "NULL" if col["nullable"] else "NOT NULL"
            default = f"DEFAULT {col['default']}" if col["default"] is not None else ""
            return f"{name} {full_type} {nullable} {default}"

        table_name = ddl["table_name"].lower()
        columns = [f"{_generate_column(col).strip()}" for col in ddl["columns"]]
        filed_def_list = ",\n  ".join(columns)
        script = f"""-- {table_name}
CREATE TABLE {table_name} (
    {filed_def_list}
);"""

        return script

    def gen_index(self, ddl: Dict) -> str:
        return "\n".join(f"{script};" for script in self.index(ddl))

    def gen_comment(self, table_ddl: Dict) -> str:
        """生成字段及表的注释"""

        script = ""
        for column in table_ddl["columns"]:
            table_comment = column["comment"]
            script += (
                f"COMMENT ON COLUMN {table_ddl['table_name']}.{column['name']} IS '{table_comment}';"
                + "\n"
            )

        table_comment = table_ddl["comment"]
        if table_comment:
            script += (
                f"COMMENT ON TABLE {table_ddl['table_name']} IS '{table_comment}';\n"
            )

        return script

    def gen_pk(self, table_name) -> str:
        """生成主键定义"""
        return ""

    def gen_uk(self, table_ddl: Dict) -> str:
        script = ""
        uk_list = list(Convertor.unique_index(table_ddl))
        for idx, (table_name, _, uk_columns) in enumerate(uk_list, 1):
            uk_name = f"uk_{table_name}_{idx:02d}"
            script += f"CREATE UNIQUE INDEX {uk_name} ON {table_name} ({', '.join(uk_columns)});\n"

        return script

    def gen_insert(self, table_name: str) -> str:
        """生成 insert 语句，以及根据最后的 insert id+1 生成 Sequence"""

        inserts = list(Convertor.inserts(table_name, self.content))
        ## 生成 insert 脚本
        script = ""
        if inserts:
            inserts_lines = "\n".join(inserts)
            script += f"""\n
{inserts_lines}"""

        return script


class OracleConvertor(Convertor):
    def __init__(self, src):
        super().__init__(src, "Oracle")

    def translate_type(self, type: str, size: Optional[Union[int, Tuple[int]]]):
        """类型转换"""
        type = type.lower()

        if type == "varchar":
            return f"varchar2({size if size < 4000 else 4000})"
        if type in ("int", "int unsigned"):
            return "number"
        if type == "bigint" or type == "bigint unsigned":
            return "number"
        if type == "datetime":
            return "date"
        if type == "timestamp":
            return f"timestamp({size})"
        if type == "bit":
            return "number(1,0)"
        if type in ("tinyint", "smallint"):
            return "smallint"
        if type in ("text", "longtext"):
            return "clob"
        if type in ("blob", "mediumblob"):
            return "blob"
        if type == "decimal":
            return (
                f"number({','.join(str(s) for s in size)})" if len(size) else "number"
            )

    def gen_create(self, ddl) -> str:
        """生成 CREATE 语句"""

        def generate_column(col):
            name = col["name"].lower()
            if name == "id":
                return "id number GENERATED ALWAYS AS IDENTITY"

            type = col["type"].lower()
            full_type = self.translate_type(type, col["size"])
            nullable = "NULL" if col["nullable"] else "NOT NULL"
            # Oracle的 INSERT '' 不能通过NOT NULL校验，因此对文字类型字段覆写为 NULL
            nullable = "NULL" if type in ("varchar", "text", "longtext") else nullable
            default = f"DEFAULT {col['default']}" if col["default"] is not None else ""
            # Oracle 中 size 不能作为字段名
            field_name = '"size"' if name == "size" else name
            # Oracle DEFAULT 定义在 NULLABLE 之前
            return f"{field_name} {full_type} {default} {nullable}"

        table_name = ddl["table_name"].lower()
        columns = [f"{generate_column(col).strip()}" for col in ddl["columns"]]
        field_def_list = ",\n    ".join(columns)
        script = f"""-- {table_name}
CREATE TABLE {table_name} (
    {field_def_list}
);"""

        # oracle INSERT '' 不能通过 NOT NULL 校验
        script = script.replace("DEFAULT '' NOT NULL", "DEFAULT '' NULL")

        return script

    def gen_index(self, ddl: Dict) -> str:
        return "\n".join(f"{script};" for script in self.index(ddl))

    def gen_comment(self, table_ddl: Dict) -> str:
        script = ""
        for column in table_ddl["columns"]:
            table_comment = column["comment"]
            script += (
                f"COMMENT ON COLUMN {table_ddl['table_name']}.{column['name']} IS '{table_comment}';"
                + "\n"
            )

        table_comment = table_ddl["comment"]
        if table_comment:
            script += (
                f"COMMENT ON TABLE {table_ddl['table_name']} IS '{table_comment}';\n"
            )

        return script

    def gen_pk(self, table_name: str) -> str:
        """生成主键定义"""
        return f"ALTER TABLE {table_name} ADD CONSTRAINT pk_{table_name} PRIMARY KEY (id);\n"

    def gen_uk(self, table_ddl: Dict) -> str:
        script = ""
        uk_list = list(Convertor.unique_index(table_ddl))
        for idx, (table_name, _, uk_columns) in enumerate(uk_list, 1):
            uk_name = f"uk_{table_name}_{idx:02d}"
            script += f"CREATE UNIQUE INDEX {uk_name} ON {table_name} ({', '.join(uk_columns)});\n"

        return script

    def gen_index(self, ddl: Dict) -> str:
        return "\n".join(f"{script};" for script in self.index(ddl))

    def gen_insert(self, table_name: str) -> str:
        """拷贝 INSERT 语句"""
        inserts = []
        for insert_script in Convertor.inserts(table_name, self.content):
            insert_script = (
                insert_script.replace("(id,", "(")
                .replace("VALUES (1,", "VALUES (")
                .replace("now(),", "sysdate,")
            )
            inserts.append(insert_script)

        ## 生成 insert 脚本
        script = ""
        if inserts:
            inserts_lines = "\n".join(inserts)
            script += f"""\n\n
{inserts_lines}"""

        return script


class SQLServerConvertor(Convertor):
    """_summary_

    Args:
        Convertor (_type_): _description_
    """

    def __init__(self, src):
        super().__init__(src, "Microsoft SQL Server")

    def translate_type(self, type: str, size: Optional[Union[int, Tuple[int]]]):
        """类型转换"""

        type = type.lower()

        if type == "varchar":
            return f"nvarchar({size if size < 4000 else 4000})"
        if type in ("int", "int unsigned"):
            return "int"
        if type in ("bigint", "bigint unsigned"):
            return "bigint"
        if type in ("datetime", "timestamp"):
            return "datetime2"
        if type == "bit":
            return "varchar(1)"
        if type in ("tinyint", "smallint"):
            return "tinyint"
        if type in ("text", "longtext"):
            return "nvarchar(max)"
        if type in ("blob", "mediumblob"):
            return "varbinary(max)"
        if type == "decimal":
            return (
                f"numeric({','.join(str(s) for s in size)})" if len(size) else "numeric"
            )

    def gen_create(self, ddl: Dict) -> str:
        """生成 create"""

        def _generate_column(col):
            name = col["name"].lower()
            if name == "id":
                return "id bigint NOT NULL PRIMARY KEY IDENTITY"

            type = col["type"].lower()
            full_type = self.translate_type(type, col["size"])
            nullable = "NULL" if col["nullable"] else "NOT NULL"
            default = f"DEFAULT {col["default"]}" if col["default"] is not None else ""
            default = re.sub(r"CURRENT_TIMESTAMP\(\d+\)", "CURRENT_TIMESTAMP", default)
            return f"{name} {full_type} {nullable} {default}"

        table_name = ddl["table_name"].lower()
        columns = [f"{_generate_column(col).strip()}" for col in ddl["columns"]]
        filed_def_list = ",\n    ".join(columns)
        script = f"""-- {table_name}
CREATE TABLE {table_name} (
    {filed_def_list}
)
GO"""

        return script

    def gen_comment(self, table_ddl: Dict) -> str:
        """生成字段及表的注释"""

        script = ""
        table_name = table_ddl["table_name"]

        for column in table_ddl["columns"]:
            column_comment = column["comment"]
            field = column["name"]

            script += f"""EXEC sp_addextendedproperty
    'MS_Description', N'{column_comment}',
    'SCHEMA', N'dbo',
    'TABLE', N'{table_name}',
    'COLUMN', N'{field}'
GO

"""

        table_comment = table_ddl["comment"]
        if table_comment:
            script += f"""EXEC sp_addextendedproperty
    'MS_Description', N'{table_comment}',
    'SCHEMA', N'dbo',
    'TABLE', N'{table_name}'
GO

"""
        return script

    def gen_pk(self, table_name: str) -> str:
        """生成主键定义"""
        return ""

    def gen_uk(self, table_ddl: Dict) -> str:
        script = ""
        uk_list = list(Convertor.unique_index(table_ddl))
        for idx, (table_name, _, uk_columns) in enumerate(uk_list, 1):
            uk_name = f"uk_{table_name}_{idx:02d}"
            script += f"CREATE UNIQUE INDEX {uk_name} ON {table_name} ({', '.join(uk_columns)})\nGO"

        return script

    def gen_index(self, ddl: Dict) -> str:
        """生成 index"""
        return "\n".join(f"{script}\nGO" for script in self.index(ddl))

    def gen_insert(self, table_name: str) -> str:
        """生成 insert 语句"""

        # 收集 `table_name` 对应的 insert 语句
        inserts = []
        for insert_script in Convertor.inserts(table_name, self.content):
            # SQLServer: 字符串前加N，hack，是否存在替换字符串内容的风险
            insert_script = (
                insert_script.replace(", '", ", N'")
                .replace("VALUES ('", "VALUES (N'")
                .replace("(id, ", "(")
                .replace("VALUES (1, ", "VALUES (")
                .replace("now(), ", "getdate(), ")
            )
            # 删除 insert 的结尾分号
            insert_script = re.sub(";$", r"\nGO", insert_script)
            inserts.append(insert_script)

        ## 生成 insert 脚本
        script = ""
        if inserts:
            inserts_lines = "\n".join(inserts)
            script += f"""\n\n
{inserts_lines}"""

        return script


class DM8Convertor(Convertor):
    def __init__(self, src):
        super().__init__(src, "DM8")

        # if type == "varchar":
        #     return f"varchar2({size if size < 4000 else 4000})"
        # if type in ("int", "int unsigned"):
        #     return "number"
        # if type == "bigint" or type == "bigint unsigned":
        #     return "number"
        # if type == "datetime":
        #     return "date"
        # if type == "timestamp":
        #     return f"timestamp({size})"
        # if type == "bit":
        #     return "number(1,0)"
        # if type in ("tinyint", "smallint"):
        #     return "smallint"
        # if type in ("text", "longtext"):
        #     return "clob"
        # if type in ("blob", "mediumblob"):
        #     return "blob"
        # if type == "decimal":
        #     return (
        #         f"number({','.join(str(s) for s in size)})" if len(size) else "number"
        #     )

    def translate_type(self, type: str, size: Optional[Union[int, Tuple[int]]]):
        """类型转换"""
        type = type.lower()

        if type == "varchar":
            return f"varchar({size})"
        if type in ("int", "int unsigned"):
            return "int"
        if type in ("bigint", "bigint unsigned"):
            return "bigint"
        if type == "datetime":
            return "datetime"
        if type == "timestamp":
            return f"timestamp({size})"
        if type == "bit":
            return "bit"
        if type in ("tinyint", "smallint"):
            return "smallint"
        if type in ("text", "longtext"):
            return "text"
        if type in ("blob", "mediumblob"):
            return "blob"
        if type == "decimal":
            return (
                f"decimal({','.join(str(s) for s in size)})" if len(size) else "decimal"
            )

    def gen_create(self, ddl) -> str:
        """生成 CREATE 语句"""

        def generate_column(col):
            name = col["name"].lower()
            if name == "id":
                return "id bigint NOT NULL PRIMARY KEY IDENTITY"

            type = col["type"].lower()
            full_type = self.translate_type(type, col["size"])
            nullable = "NULL" if col["nullable"] else "NOT NULL"
            # Oracle的 INSERT '' 不能通过NOT NULL校验，因此对文字类型字段覆写为 NULL
            nullable = "NULL" if type in ("varchar", "text", "longtext") else nullable
            default = f"DEFAULT {col['default']}" if col["default"] is not None else ""
            # Oracle 中 size 不能作为字段名
            field_name = '"size"' if name == "size" else name
            # Oracle DEFAULT 定义在 NULLABLE 之前
            return f"{field_name} {full_type} {default} {nullable}"

        table_name = ddl["table_name"].lower()
        columns = [f"{generate_column(col).strip()}" for col in ddl["columns"]]
        field_def_list = ",\n    ".join(columns)
        script = f"""-- {table_name}
CREATE TABLE {table_name} (
    {field_def_list}
);"""

        # oracle INSERT '' 不能通过 NOT NULL 校验
        # script = script.replace("DEFAULT '' NOT NULL", "DEFAULT '' NULL")

        return script

    def gen_index(self, ddl: Dict) -> str:
        return "\n".join(f"{script};" for script in self.index(ddl))

    def gen_comment(self, table_ddl: Dict) -> str:
        script = ""
        for column in table_ddl["columns"]:
            table_comment = column["comment"]
            script += (
                f"COMMENT ON COLUMN {table_ddl['table_name']}.{column['name']} IS '{table_comment}';"
                + "\n"
            )

        table_comment = table_ddl["comment"]
        if table_comment:
            script += (
                f"COMMENT ON TABLE {table_ddl['table_name']} IS '{table_comment}';\n"
            )

        return script

    def gen_pk(self, table_name: str) -> str:
        """生成主键定义"""
        return ""

    def gen_uk(self, table_ddl: Dict) -> str:
        script = ""
        uk_list = list(Convertor.unique_index(table_ddl))
        for idx, (table_name, _, uk_columns) in enumerate(uk_list, 1):
            uk_name = f"uk_{table_name}_{idx:02d}"
            script += f"CREATE UNIQUE INDEX {uk_name} ON {table_name} ({', '.join(uk_columns)});\n"

        return script

    def gen_index(self, ddl: Dict) -> str:
        return "\n".join(f"{script};" for script in self.index(ddl))

    def gen_insert(self, table_name: str) -> str:
        """拷贝 INSERT 语句"""
        inserts = []
        for insert_script in Convertor.inserts(table_name, self.content):
            insert_script = (
                insert_script.replace("(id,", "(")
                .replace("VALUES (1,", "VALUES (")
                .replace("now(),", "sysdate,")
            )
            inserts.append(insert_script)

        ## 生成 insert 脚本
        script = ""
        if inserts:
            inserts_lines = "\n".join(inserts)
            script += f"""\n\n
{inserts_lines}"""

        return script


def main():
    parser = argparse.ArgumentParser(description="Snail Job Database Transfer Tool")
    parser.add_argument(
        "type",
        type=str,
        help="Target database type",
        choices=["postgre", "oracle", "sqlserver", "dm8"],
    )
    args = parser.parse_args()

    sql_file = pathlib.Path("../sql/snail_job_mysql.sql").resolve().as_posix()
    convertor = None
    if args.type == "postgre":
        convertor = PostgreSQLConvertor(sql_file)
    elif args.type == "oracle":
        convertor = OracleConvertor(sql_file)
    elif args.type == "sqlserver":
        convertor = SQLServerConvertor(sql_file)
    elif args.type ==  "dm8":
        convertor = DM8Convertor(sql_file)
    else:
        raise NotImplementedError(f"Database type not supported: {args.type}")

    convertor.print()


if __name__ == "__main__":
    main()
