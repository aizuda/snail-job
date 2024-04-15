package com.aizuda.snail.job.client.core.serializer;

import com.aizuda.snail.job.client.core.RetryArgSerializer;
import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import cn.hutool.core.util.StrUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Objects;

/**
 * Hessian序列化
 *
 * @author: opensnail
 * @date : 2022-03-07 15:08
 */
public class HessianSerializer implements RetryArgSerializer {

    @Override
    public String serialize(Object t) {
        if (Objects.isNull(t)) {
            return StrUtil.EMPTY;
        }

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            HessianOutput ho = new HessianOutput(os);
            ho.writeObject(t);
            return Base64.getEncoder().encodeToString(os.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("HessianSerializationConverter.serialize failed.", e);
        }
    }

    @Override
    public Object deSerialize(String infoStr, Class aClass, Method method) {
        if (StrUtil.isBlank(infoStr)) {
            return null;
        }

        byte[] convertBytes = Base64.getDecoder().decode(infoStr);
        try (ByteArrayInputStream is = new ByteArrayInputStream(convertBytes)) {
            HessianInput hi = new HessianInput(is);
            return hi.readObject(Object[].class);
        } catch (IOException e) {
            throw new IllegalStateException("HessianSerializationConverter.deSerialize failed.", e);
        }
    }
}
