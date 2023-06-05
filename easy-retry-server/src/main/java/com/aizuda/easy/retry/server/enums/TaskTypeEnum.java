package com.aizuda.easy.retry.server.enums;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.server.akka.ActorGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author www.byteblogs.com
 * @date 2023-06-04
 * @since 2.0
 */
@AllArgsConstructor
@Getter
public enum TaskTypeEnum {
    RETRY(1, ActorGenerator.scanGroupActor()),
    CALLBACK(2, ActorGenerator.scanCallbackGroupActor());

    private final Integer type;
    private final ActorRef actorRef;
}
