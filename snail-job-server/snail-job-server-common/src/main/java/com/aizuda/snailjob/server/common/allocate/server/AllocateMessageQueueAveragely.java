package com.aizuda.snailjob.server.common.allocate.server;

import com.aizuda.snailjob.server.common.ServerLoadBalance;

import java.util.ArrayList;
import java.util.List;

/**
 * 通过平均算法分配bucketList
 *
 * @author opensnail
 * @date 2021-03-01
 */
@SuppressWarnings({"squid:S3358"})
public class AllocateMessageQueueAveragely implements ServerLoadBalance<Integer, String> {

    @Override
    public List<Integer> allocate(String currentCID, List<Integer> bucketList, List<String> serverList) {

        List<Integer> consumerBucket = new ArrayList<>();

        // 找到当前消费者在消费者队列里面的下标
        int index = serverList.indexOf(currentCID);

        // 此处取余是为了判断队列与总消费者数的是否是整数倍 mod=0则是整数倍，否则不是
        int mod = bucketList.size() % serverList.size();

        /**
         * 下面三目预算详解为
         * 如果队列 <= 总消费者, 这种情况最简单就是平均每个消费者消费一个队列，averageSize = 1
         * 如果队列 > 总消费者,这里分为 mod是否等于0，
         *    mod=0, 说明是队列与总消费者数是整数倍，只需要平均分配就好，eg:10/5=2
         *    mode>0, 说明队列与总消费者数不是整数倍，这里肯定有的消费者消费的多，eg: 10/8=1余2
         *        index < mod 这个意思是消费者的下标小于mod的， 多消费一个，eg: 队列10个 消费者8个 mod=2 ,那么下标0消费2个、下标1消费2个，其余消费1个
         *        index >= mod 其余消费者平均消费剩下的队列
         */
        int averageSize =
            bucketList.size() <= serverList.size() ? 1
                : (mod > 0 && index < mod ? bucketList.size() / serverList.size()
                    + 1 : bucketList.size() / serverList.size());

        // 这里开始计算起始坐标
        int startIndex = (mod > 0 && index < mod) ? index * averageSize : index * averageSize + mod;

        /**
         * 取最小值为消费队列的范围
         * 这里需要说明，如果队列 < 消费者下标， range是一个小于0的数字，所以我们的消费者的数量应该是小于等队列数量，否则会造成资源浪费
         */
        int range = Math.min(averageSize, bucketList.size() - startIndex);
        for (int i = 0; i < range; i++) {
            consumerBucket.add(bucketList.get((startIndex + i) % bucketList.size()));
        }

        return consumerBucket;
    }

    @Override
    public String getName() {
        return "AVG";
    }
}
