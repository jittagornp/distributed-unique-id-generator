package me.jittagornp.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.BasicConfigurator;

import java.security.SecureRandom;
import java.time.Instant;

@Slf4j
public class IdGeneratorTest {

    public static void main(final String[] args) {

        BasicConfigurator.configure();

        final int MAX_GROUP = 31;
        final int MAX_SHARD = 31;

        final SecureRandom secureRandom = new SecureRandom();
        final long group = secureRandom.nextInt() & MAX_GROUP;
        final long shard = secureRandom.nextInt() & MAX_SHARD;
        final long epochMilli = Instant.now().toEpochMilli();

        final IdGenerator idGenerator = new IdGeneratorImpl(group, shard, epochMilli);

        for (int i = 0; i < 100; i++) {
            final long id = idGenerator.nextId();
            final String binary = leftPad0(Long.toBinaryString(id), 64);
            log.debug("id => {}, binary => {}", id, binary);
        }

    }

    private static String leftPad0(final String src, final int size) {
        final int length = src.length();
        final StringBuilder builder = new StringBuilder();
        for (int i = length; i < size; i++) {
            builder.append("0");
        }
        return builder.append(src).toString();
    }

}
