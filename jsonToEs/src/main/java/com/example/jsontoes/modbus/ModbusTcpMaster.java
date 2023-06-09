package com.example.jsontoes.modbus;

import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.ip.IpParameters;


public class ModbusTcpMaster {
    private static ModbusFactory modbusFactory;

    static {
        if (modbusFactory == null) {
            modbusFactory = new ModbusFactory();
        }
    }

    /**
     * 获取Tcp master
     * @param ip
     * @param port
     * @return
     */
    public static ModbusMaster getMaster(String ip, int port) {
        IpParameters params = new IpParameters();
        params.setHost(ip);
        params.setPort(port);
        //这个属性确定了协议帧是否是通过tcp封装的RTU结构，采用modbus tcp/ip时，要设为false, 采用modbus rtu over tcp/ip时，要设为true
        params.setEncapsulated(false);
        // 参数1：IP和端口信息 参数2：保持连接激活
        ModbusMaster master = null;
        master = modbusFactory.createTcpMaster(params, true);
        try {
            //设置超时时间
            master.setTimeout(500);
            //设置重连次数
            master.setRetries(2);
            //初始化
            master.init();
        } catch (ModbusInitException e) {
            e.printStackTrace();
        }
        return master;
    }
}
