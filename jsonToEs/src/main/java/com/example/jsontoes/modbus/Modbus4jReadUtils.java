package com.example.jsontoes.modbus;

import com.serotonin.modbus4j.BatchRead;
import com.serotonin.modbus4j.BatchResults;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.locator.BaseLocator;
import com.serotonin.modbus4j.msg.*;


public class Modbus4jReadUtils {

    /**
     * 读（线圈）开关量数据
     * 功能码为：01； 读取开关量输出点的ON/OFF状态,可以读写的布尔类型(0x)---00001 至 0xxxx – 开关量输出
     * @param slaveId slaveId-从站编号-自行约定
     * @param offset  位置
     * @return 读取值-读取多少个
     */
    public boolean[] readCoilStatus(ModbusMaster master,int slaveId, int offset, int numberOfBits)
            throws ModbusTransportException, ErrorResponseException, ModbusInitException {

        ReadCoilsRequest request = new ReadCoilsRequest(slaveId, offset, numberOfBits);
        ReadCoilsResponse response = (ReadCoilsResponse) master.send(request);
        boolean[] booleans = response.getBooleanData();
        return valueRegroup(numberOfBits, booleans);
    }


    /**开关数据 读取外围设备输入的开关量
     * 功能码为：02；读取开关量输入点的ON/OFF状态,只能读的布尔类型(1x)---10001 至 1xxxx – 开关量输入
     * @param slaveId-从站编号-自行约定
     * @param offset-预访问的地址-地址范围：0-255
     * @param numberOfBits-读取多少个
     * @return
     * @throws ModbusTransportException
     * @throws ErrorResponseException
     * @throws ModbusInitException
     */
    public boolean[] readInputStatus(ModbusMaster master,int slaveId, int offset, int numberOfBits)
            throws ModbusTransportException, ErrorResponseException, ModbusInitException {
        ReadDiscreteInputsRequest request = new ReadDiscreteInputsRequest(slaveId, offset, numberOfBits);
        ReadDiscreteInputsResponse response = (ReadDiscreteInputsResponse) master.send(request);
        boolean[] booleans = response.getBooleanData();
        return valueRegroup(numberOfBits, booleans);
    }

    /**
     * 读取保持寄存器数据
     * 功能码为：03 读取保持寄存器的数据,可以读写的数字类型(4x)---40001 至 4xxxx – 保持寄存器
     *
     **举例子说明：S7-200
     Smart PLC中，设置  [HoldStr~]=&VB1000;则对应的保持寄存器地址为VW1000\VW1002\VW10004
     **在java中对应的address为：0、1、2
     * @param slaveId slave Id-从站编号-自行约定
     * @param offset  位置
     * @param numberOfBits numberOfRegisters 寄存器个数  每个寄存器表示一个16位无符号整数 相当于一个short
     */
    public static short[] readHoldingRegister(ModbusMaster master,int slaveId, int offset, int numberOfBits)
            throws ModbusTransportException, ErrorResponseException, ModbusInitException {
        ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(slaveId, offset, numberOfBits);
        ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) master.send(request);
        return response.getShortData();
    }


    /**
     * 读取[03 Holding Register类型 2x]模拟量数据
     * @param slaveId slave Id
     * @param offset 位置
     * @param dataType 数据类型,来自com.serotonin.modbus4j.code.DataType
     * @return
     * @throws ModbusTransportException 异常
     * @throws ErrorResponseException 异常
     * @throws ModbusInitException 异常
     */
    public static Number readHoldingRegisterByDataType(ModbusMaster master,int slaveId, int offset, int dataType)
            throws ModbusTransportException, ErrorResponseException, ModbusInitException {
        // 03 Holding Register类型数据读取
        BaseLocator<Number> loc = BaseLocator.holdingRegister(slaveId, offset, dataType);
        Number value = master.getValue(loc);
        return value;
    }

    /**
     * 读取外围设备输入的数据
     * 功能码为：04 读取模拟量输入值，只能读的数字类型(3x)---30001 至 3xxxx – 模拟量输入
     *
     * 举例子说明：S7-200 Smart PLC中，模拟量输入寄存器AIW16\AIW18,则对应
     * java中对应的address为：8\9
     * @param slaveId slaveId-从站编号-自行约定
     * @param offset  位置-预访问的地址-地址范围：0-55
     */
    public short[] readInputRegisters(ModbusMaster master,int slaveId, int offset, int numberOfBits)
            throws ModbusTransportException, ErrorResponseException, ModbusInitException {
        ReadInputRegistersRequest request = new ReadInputRegistersRequest(slaveId, offset, numberOfBits);
        ReadInputRegistersResponse response = (ReadInputRegistersResponse) master.send(request);
        return response.getShortData();
    }

    /**
     * 批量读取 可以批量读取不同寄存器中数据
     */
    public static void batchRead(ModbusMaster master) throws ModbusTransportException, ErrorResponseException, ModbusInitException {
        BatchRead<Integer> batch = new BatchRead<Integer>();
        batch.addLocator(0, BaseLocator.holdingRegister(1, 1, DataType.TWO_BYTE_INT_SIGNED));
        batch.addLocator(1, BaseLocator.inputStatus(1, 0));
        batch.setContiguousRequests(true);
        BatchResults<Integer> results = master.send(batch);
        System.out.println("batchRead:" + results.getValue(0));
        System.out.println("batchRead:" + results.getValue(1));
    }


    /**
     * 批量读取 可以批量读取不同寄存器中数据
     */
    public static void batchReadTest(ModbusMaster master,int slaveId, int offset, int dataType) throws ModbusTransportException, ErrorResponseException, ModbusInitException {
        BatchRead<Integer> batch = new BatchRead<Integer>();
//        BaseLocator<Number> loc = BaseLocator.holdingRegister(slaveId, offset, dataType);
//        Number value = master.getValue(loc);
        batch.addLocator(0, BaseLocator.holdingRegister(1, 2, DataType.TWO_BYTE_INT_SIGNED));
        batch.addLocator(1, BaseLocator.inputStatus(1, 0));
        batch.addLocator(2, BaseLocator.holdingRegister(slaveId, offset, dataType));
        batch.setContiguousRequests(true);
        BatchResults<Integer> results = master.send(batch);
        System.out.println("batchRead:" + results.getValue(0));
        System.out.println("batchRead:" + results.getValue(1));
        System.out.println("batchRead:" + results.getValue(2));
    }


    /**
     * 数据重组
     * @param numberOfBits
     * @param values
     * @return
     */
    private boolean[] valueRegroup(int numberOfBits, boolean[] values) {
        boolean[] bs = new boolean[numberOfBits];
        int temp = 1;
        for (boolean b : values) {
            bs[temp - 1] = b;
            temp++;
            if (temp > numberOfBits)
                break;
        }
        return bs;
    }
}
