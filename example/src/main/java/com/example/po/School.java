package com.example.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * <p>
 * 学校
 * </p>
 *
 * @author www.byteblogs.com
 * @since 2022-03-24
 */
@Data
public class School implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String address;

    private LocalDateTime createDt;

    private LocalDateTime updateDt;

}
