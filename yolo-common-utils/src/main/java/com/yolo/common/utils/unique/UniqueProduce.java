package com.yolo.common.utils.unique;

import java.util.Date;

public class UniqueProduce {
	private long workerId;
    private long datacenterId;
    private long sequence = 0L;

    private static long twepoch = 1507798531131L;

    private static long workerIdBits = 5L;
    private static long datacenterIdBits = 5L;
    private static long maxWorkerId = -1L ^ (-1L << (int)workerIdBits);
    private static long maxDatacenterId = -1L ^ (-1L << (int)datacenterIdBits);
    private static long sequenceBits = 12L;

    private long workerIdShift = sequenceBits;
    private long datacenterIdShift = sequenceBits + workerIdBits;
    private long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    private long sequenceMask = -1L ^ (-1L << (int)sequenceBits);

    private long lastTimestamp = -1L;
    private static Object syncRoot = new Object();

    public UniqueProduce(long workerId, long datacenterId)throws Exception
    {

        // sanity check for workerId
        if (workerId > maxWorkerId || workerId < 0)
        {
            throw new Exception("worker Id can't be greater than "+maxWorkerId+" or less than 0");
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0)
        {
            throw new Exception("datacenter Id can't be greater than "+maxDatacenterId+" or less than 0");
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public long nextId()throws Exception
    {
        synchronized (syncRoot)
        {
            long timestamp = timeGen();

            if (timestamp < lastTimestamp)
            {
                throw new Exception("Clock moved backwards.  Refusing to generate id for "+(lastTimestamp - timestamp)+" milliseconds");
            }

            if (lastTimestamp == timestamp)
            {
                sequence = (sequence + 1) & sequenceMask;
                if (sequence == 0)
                {
                    timestamp = tilNextMillis(lastTimestamp);
                }
            }
            else
            {
                sequence = 0L;
            }

            lastTimestamp = timestamp;

            return ((timestamp - twepoch) << (int)timestampLeftShift) | (datacenterId << (int)datacenterIdShift) | (workerId << (int)workerIdShift) | sequence;
        }
    }

    protected long tilNextMillis(long lastTimestamp)
    {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp)
        {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long timeGen()
    {
        return new Date().getTime();
    }
    
    public static void main(String[] args) throws Exception{
    	UniqueProduce idWorker1 = new UniqueProduce(0, 0);
    	int count=0;
    	while (count<1000)
        {
            long id = idWorker1.nextId();
            System.out.println(id);
            count++;
        }
    	System.out.println(new Date().getTime());
	}
}
