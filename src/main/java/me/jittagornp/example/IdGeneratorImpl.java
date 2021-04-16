package me.jittagornp.example;

import lombok.extern.slf4j.Slf4j;
import java.security.SecureRandom;
import java.time.Instant;

@Slf4j
public class IdGeneratorImpl implements IdGenerator {

    private final static int MAX_BIT_SIZE = 64;

    private final static int SIGN_BIT_SIZE = 1;
    private final static int EPOCH_BIT_SIZE = 41;
    private final static int GROUP_BIT_SIZE = 5;
    private final static int SHARD_BIT_SIZE = 5;
    private final static int SEQUENCE_BIT_SIZE = 12;

    private final static int EPOCH_SHIFT_BIT_SIZE = MAX_BIT_SIZE - (SIGN_BIT_SIZE + EPOCH_BIT_SIZE);
    private final static int GROUP_SHIFT_BIT_SIZE = MAX_BIT_SIZE - (SIGN_BIT_SIZE + EPOCH_BIT_SIZE + GROUP_BIT_SIZE);
    private final static int SHARD_SHIFT_BIT_SIZE = MAX_BIT_SIZE - (SIGN_BIT_SIZE + EPOCH_BIT_SIZE + GROUP_BIT_SIZE + SHARD_BIT_SIZE);
    private final static int SEQUENCE_SHIFT_BIT_SIZE = MAX_BIT_SIZE - (SIGN_BIT_SIZE + EPOCH_BIT_SIZE + GROUP_BIT_SIZE + SHARD_BIT_SIZE + SEQUENCE_BIT_SIZE);

    //2,199,023,255,551 values
    private final static long MAX_EPOCH = (long) (Math.pow(2, EPOCH_BIT_SIZE) - 1);

    //31 groups
    private final static int MAX_GROUP = (int) (Math.pow(2, GROUP_BIT_SIZE) - 1);

    //31 shards
    private final static int MAX_SHARD = (int) (Math.pow(2, SHARD_BIT_SIZE) - 1);

    //4,097 sequences
    private final static int MAX_SEQUENCE = (int) (Math.pow(2, SEQUENCE_BIT_SIZE) - 1);

    private volatile long lastEpochMilli = -1L;
    private volatile long sequence = 0L;
    //
    private long group;
    private long shard;
    private long customEpochMilli;

    public IdGeneratorImpl() {
        //Random group and shard
        this(
                new SecureRandom().nextLong() & MAX_GROUP,
                new SecureRandom().nextLong() & MAX_SHARD,
                0
        );
    }

    public IdGeneratorImpl(long group, long shard, long customEpochMilli) {
        if (group > MAX_GROUP) {
            throw new IllegalArgumentException(String.format("Group must between %d and %d", 0, MAX_GROUP));
        }
        if (shard > MAX_SHARD) {
            throw new IllegalArgumentException(String.format("Shard must between %d and %d", 0, MAX_SHARD));
        }
        this.group = group;
        this.shard = shard;
        this.customEpochMilli = customEpochMilli;

        log.info("MAX_BIT_SIZE => {}", MAX_BIT_SIZE);
        log.info("SIGN_BIT_SIZE => {}", SIGN_BIT_SIZE);
        log.info("EPOCH_BIT_SIZE => {}", EPOCH_BIT_SIZE);
        log.info("GROUP_BIT_SIZE => {}", GROUP_BIT_SIZE);
        log.info("SHARD_BIT_SIZE => {}", SHARD_BIT_SIZE);
        log.info("SEQUENCE_BIT_SIZE => {}", SEQUENCE_BIT_SIZE);
        log.info("EPOCH_SHIFT_BIT_SIZE => {}", EPOCH_SHIFT_BIT_SIZE);
        log.info("GROUP_SHIFT_BIT_SIZE => {}", GROUP_SHIFT_BIT_SIZE);
        log.info("SHARD_SHIFT_BIT_SIZE => {}", SHARD_SHIFT_BIT_SIZE);
        log.info("SEQUENCE_SHIFT_BIT_SIZE => {}", SEQUENCE_SHIFT_BIT_SIZE);
        log.info("MAX_EPOCH => {}", MAX_EPOCH);
        log.info("MAX_GROUP => {}", MAX_GROUP);
        log.info("MAX_SHARD => {}", MAX_SHARD);
        log.info("MAX_SEQUENCE => {}", MAX_SEQUENCE);
        log.info("CUSTOM_EPOCH => {}, Date Time => {}", customEpochMilli, Instant.ofEpochMilli(customEpochMilli));
        log.info("group => {}", group);
        log.info("shard => {}", shard);
    }

    private long systemCurrentEpochMilli() {
        return (Instant.now().toEpochMilli() - customEpochMilli) & MAX_EPOCH;
    }

    @Override
    public synchronized long nextId() {
        long currentEpochMilli = systemCurrentEpochMilli();
        if (currentEpochMilli < lastEpochMilli) {
            throw new IllegalStateException("Invalid System Clock");
        }

        if (currentEpochMilli == lastEpochMilli) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                //Block until next milliseconds
                while (currentEpochMilli <= lastEpochMilli) {
                    currentEpochMilli = systemCurrentEpochMilli();
                }
            }
        } else {
            sequence = 0;
        }

        lastEpochMilli = currentEpochMilli;

        long id = (currentEpochMilli << EPOCH_SHIFT_BIT_SIZE);
        id = id | (group << GROUP_SHIFT_BIT_SIZE);
        id = id | (shard << SHARD_SHIFT_BIT_SIZE);
        id = id | (sequence << SEQUENCE_SHIFT_BIT_SIZE);
        return id;
    }
}