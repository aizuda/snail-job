package com.aizuda.easy.retry.server.common.enums;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Supplier;

/**
 * @author www.byteblogs.com
 * @date 2023-06-04
 * @since 2.0
 */
@AllArgsConstructor
@Getter
public enum TaskTypeEnum {
    RETRY(1, ActorGenerator::scanGroupActor),
    CALLBACK(2, ActorGenerator::scanCallbackGroupActor),
    JOB(3, ActorGenerator::scanJobActor),
    ;


    private final Integer type;
    private final Supplier<ActorRef> actorRef;

}
