package com.aizuda.easy.retry.client.core.serializer;

import com.aizuda.easy.retry.client.core.RetryArgSerializer;
import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Objects;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 15:08
 * @date 2022/1/5 11:14 上午
 */
@Component("XRetryHessianSerializer")
public class HessianSerializer implements RetryArgSerializer {

    @Override
    public String serialize(Object t) {
        if (Objects.isNull(t)) {
            return StringUtils.EMPTY;
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
    public Object deSerialize(String infoStr, Class aClass, Method method) throws JsonProcessingException {
        if (StringUtils.isBlank(infoStr)) {
            return null;
        }

        byte[] convertBytes = Base64.getDecoder().decode(infoStr);
        try (ByteArrayInputStream is = new ByteArrayInputStream(convertBytes)) {
            HessianInput hi = new HessianInput(is);
            return hi.readObject(aClass);
        } catch (IOException e) {
            throw new IllegalStateException("HessianSerializationConverter.deSerialize failed.", e);
        }
    }
}
