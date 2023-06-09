package com.example.jsontoes.modbus;

import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.msg.ModbusRequest;
import com.serotonin.modbus4j.msg.ModbusResponse;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest;
import com.serotonin.modbus4j.msg.WriteRegistersRequest;
import com.serotonin.modbus4j.sero.util.queue.ByteQueue;
import groovy.util.logging.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

@Slf4j
public class Modbus4jUtils {
    /**
     * 工厂。
     */
    static ModbusFactory modbusFactory;

    //建立链接
    static public ModbusMaster tcpMaster;

    static {
        if (modbusFactory == null) {
            modbusFactory = new ModbusFactory();
            tcpMaster = getMaster("192.168.1.28", 502);  // 填写要连接的TCP server
        }
    }

    /**
     * 获取master
     *
     * @return
     * @throws ModbusInitException
     */
    public static ModbusMaster getMaster(String ip, Integer port) {
        IpParameters params = new IpParameters();
        params.setHost(ip);
        params.setPort(port);

        // RTU 协议-设备是modbus rtu就用他 注意哦
        // params.setEncapsulated(true);
        // ModbusMaster master = modbusFactory.createTcpMaster(params, true);
        // TCP 协议
        ModbusMaster master = modbusFactory.createTcpMaster(params, false);

        try {
            //设置超时时间---发现设置他没用，还会报错，所以屏蔽
            // master.setTimeout(5000);
            //设置重连次数---发现设置他没用，还会报错，所以屏蔽
            //master.setRetries(3);
            //初始化
            master.init();
        } catch (ModbusInitException e) {
            e.printStackTrace();
        }
        return master;
    }

    /**
     * 读取保持寄存器 功能码[03]
     *
     * @param start     开始地址
     * @param readLenth 读取数量
     * @return
     * @throws ModbusInitException
     */
    public static ByteQueue modbusTCP03(int slaveId, ModbusMaster tcpMaster, int start, int readLenth) throws ModbusInitException {
        //发送请求
        ModbusRequest modbusRequest = null;
        try {
            modbusRequest = new ReadHoldingRegistersRequest(slaveId, start, readLenth);//功能码03
        } catch (ModbusTransportException e) {
            e.printStackTrace();
        }
        //收到响应
        ModbusResponse modbusResponse = null;
        try {
            modbusResponse = tcpMaster.send(modbusRequest);
        } catch (ModbusTransportException e) {
            e.printStackTrace();
        }
        ByteQueue byteQueue = new ByteQueue(12);
        modbusResponse.write(byteQueue);
        System.out.println("功能码:" + modbusRequest.getFunctionCode());
        System.out.println("从站地址:" + modbusRequest.getSlaveId());
        System.out.println("开始地址:" + start);
        System.out.println("收到的响应信息大小:" + byteQueue.size());
        System.out.println("收到的响应信息值:" + byteQueue);
        return byteQueue;
    }

    /*
     * 写 多个寄存器 功能码[16] - 相当厂家文档写单个和多个。一样可以
     * WriteCoilRequest  05
     * WriteRegisterRequest 06
     * WriteRegistersRequest 16
     *
     */
    public static ByteQueue modbusTCP16(int slaveId, ModbusMaster tcpMaster, int writeOffset, short[] data) throws Exception {
        WriteRegistersRequest writeRegistersRequest = null;
        //收到响应
        ModbusResponse modbusResponse = null;
        try {
            writeRegistersRequest = new WriteRegistersRequest(slaveId, writeOffset, data);
            modbusResponse = tcpMaster.send(writeRegistersRequest);
            if (modbusResponse.isException()) {
                System.out.println("Exception response: message=" + modbusResponse.getExceptionMessage());
            } else {
                System.out.println("Success");
            }
        } catch (ModbusTransportException e) {
            e.printStackTrace();
        }
        ByteQueue byteQueue = new ByteQueue(12);
        modbusResponse.write(byteQueue);
        System.out.println("功能码:" + writeRegistersRequest.getFunctionCode());
        System.out.println("从站地址:" + writeRegistersRequest.getSlaveId());
        System.out.println("收到的响应信息大小:" + byteQueue.size());
        System.out.println("收到的响应信息值:" + byteQueue);
        return byteQueue;
    }


    public static void main(String[] args) {
        try {
            // start 需要将16进制转10机制哦
            int start = 0;
            // 设备通讯地址-看情况需要转换会10机制哦
            int slaveId = 1;
            // 读 电表
            start = get16to10("0010"); //  总电能-这个值看厂家文档
            // 参数 2 是2个寄存器-这个值看厂家文档
            ByteQueue byteQueue = modbusTCP03(slaveId , tcpMaster, 2, 2);
            // 这是把 '响应的报文' 做一些转换，更多转换参考文章下面
            byte[] bytes1 = byteQueue.peekAll();
            // 这里是对 '响应的报文' 截取后面4位
            byte[] bytes2 = Arrays.copyOfRange(bytes1, bytes1.length - 4, bytes1.length);
            System.out.println(Arrays.toString(bytes2));
            System.out.println(toDouble(bytes2));

            // 写 电表
            start = get16to10("aa01"); // 电器控制值-这个值看厂家文档
            short[] fenZha = {0x22}; // 分闸-这个值看厂家文档
            // short[] hrzha = {(short) 0b1111111111111111}; // 合闸-这个值看厂家文档 是这个0x01/0xff ，但是不行！
            ByteQueue byteQueueWrite = modbusTCP16(slaveId, tcpMaster, 3, fenZha);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将byte数组转换为浮点数
     */
    public static double toDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getFloat();
    }

    /**
     * 将byte数组转换为浮点数-并保留最后两位小数点
     */
    public static double toDouble00(byte[] bytes) {
        double f = ByteBuffer.wrap(bytes).getFloat();
        return toDouble00(f);
    }

    /**
     * double 保留最后两位小数点
     */
    public static double toDouble00(double f) {
        BigDecimal b = new BigDecimal(f);
        // 是小数点后只有两位的双精度类型数据
        double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return f1;
    }

    /**
     * 将16进制转10进制-s去掉前缀0x
     * 也可以手动改后面16参数，改自己要的字节
     */
    public static int get16to10(String s) {
        return Integer.parseInt(s, 16);
    }

    /**
     * 将byte[]转为各种进制的字符串
     *
     * @param bytes byte[]
     * @param radix 基数可以转换进制的范围，从Character.MIN_RADIX到Character.MAX_RADIX，超出范围后变为10进制
     * @return 转换后的字符串
     * <p>
     * 类型 占用 bit（位）
     * byte（字节） 8
     * short（短整型） 16
     * int（整型） 32
     * long（长整型） 64
     * float（单精度浮点型） 32
     * double（双精度浮点型） 64
     * char（字符） 16
     * boolean（布尔型） 1
     */
    public static String binary(byte[] bytes, int radix) {
        return new BigInteger(1, bytes).toString(radix);// 这里的1代表正数
    }

    /**
     * 将byte数组转换为整数
     * 转换为bit后，最左边的那位表示，符号位（有符号/无符号）
     */
    public static int bytesToInt(byte[] bs) {
        int a = 0;
        for (int i = bs.length - 1; i >= 0; i--) {
            a += bs[i] * Math.pow(255, bs.length - i - 1);
        }
        return a;
    }

    /**
     * 将byte数组转换为short
     */
    public static short bytesToshort(byte[] b) {
        short l = 0;
        for (int i = 0; i < 2; i++) {
            l <<= 8; //<<=和我们的 +=是一样的，意思就是 l = l << 8
            l |= (b[i] & 0xff); //和上面也是一样的  l = l | (b[i]&0xff)
        }
        return l;
    }


}

