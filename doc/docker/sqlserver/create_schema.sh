#!/usr/bin/env bash

/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P 'EasyRetry@24' -Q "CREATE DATABASE easy_retry;
GO"
/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P 'EasyRetry@24' -d easy_retry -i /tmp/schema.sql
