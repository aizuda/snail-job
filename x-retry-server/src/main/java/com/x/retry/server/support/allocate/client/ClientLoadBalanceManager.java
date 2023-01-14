package com.x.retry.server.support.allocate.client;

import com.x.retry.server.support.ClientLoadBalance;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-12 10:20
 */
@Component
public class ClientLoadBalanceManager {

    public static ClientLoadBalance getClientLoadBalance(int routeType) {

        for (AllocationAlgorithmEnum algorithmEnum : AllocationAlgorithmEnum.values()) {
            if (algorithmEnum.getType() == routeType) {
                return algorithmEnum.getClientLoadBalance();
            }
        }

        return null;
    }

    @Getter
    public enum AllocationAlgorithmEnum {

        CONSISTENT_HASH(1, new ClientLoadBalanceConsistentHash(100)),
        RANDOM(2, new ClientLoadBalanceRandom()),
        LRU(3, new ClientLoadBalanceLRU(100)),
        ;

        private final int type;
        private final ClientLoadBalance clientLoadBalance;

        AllocationAlgorithmEnum(int type, ClientLoadBalance clientLoadBalance) {
            this.type = type;
            this.clientLoadBalance = clientLoadBalance;
        }
    }

}
