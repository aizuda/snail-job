package com.aizuda.snailjob.server.common.enums;

import  org.apache.pekko.actor.ActorRef;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Supplier;

/**
 * @author opensnail
 * @date 2023-06-04
 * @since 2.0
 */
@AllArgsConstructor
@Getter
public enum SyetemTaskTypeEnum {
    RETRY(1, ActorGenerator::scanRetryActor),
    CALLBACK(2, ActorGenerator::scanCallbackGroupActor),
    JOB(3, ActorGenerator::scanJobActor),
    WORKFLOW(4, ActorGenerator::scanWorkflowActor),
    ;


    private final Integer type;
    private final Supplier<ActorRef> actorRef;

}
