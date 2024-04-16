package com.aizuda.snailjob.server.common.allocate.client;

import com.aizuda.snailjob.server.common.ClientLoadBalance;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import lombok.Getter;

/**
 * @author: opensnail
 * @date : 2022-03-12 10:20
 */
public class ClientLoadBalanceManager {

    public static ClientLoadBalance getClientLoadBalance(int routeType) {

        for (AllocationAlgorithmEnum algorithmEnum : AllocationAlgorithmEnum.values()) {
            if (algorithmEnum.getType() == routeType) {
                return algorithmEnum.getClientLoadBalance();
            }
        }

        throw new SnailJobServerException("routeType is not existed. routeType:[{}]", routeType);
    }

    @Getter
    public enum AllocationAlgorithmEnum {

        CONSISTENT_HASH(1, new ClientLoadBalanceConsistentHash(100)),
        RANDOM(2, new ClientLoadBalanceRandom()),
        LRU(3, new ClientLoadBalanceLRU(100)),
        ROUND(4, new ClientLoadBalanceRound())
        ;

        private final int type;
        private final ClientLoadBalance clientLoadBalance;

        AllocationAlgorithmEnum(int type, ClientLoadBalance clientLoadBalance) {
            this.type = type;
            this.clientLoadBalance = clientLoadBalance;
        }
    }

}
