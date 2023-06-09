package com.example.jsontoes;

import com.example.jsontoes.modbus.Modbus4jReadUtils;
import com.example.jsontoes.modbus.ModbusTcpMaster;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JsonToEsApplicationTests {

    @Test
    void contextLoads() {
        ModbusMaster master = ModbusTcpMaster.getMaster("192.168.1.28", 502);
        try {
            short[] shorts = Modbus4jReadUtils.readHoldingRegister(master, 1, 1, 1);
            System.out.println(shorts.toString());
        } catch (ModbusTransportException e) {
            e.printStackTrace();
        } catch (ErrorResponseException e) {
            e.printStackTrace();
        } catch (ModbusInitException e) {
            e.printStackTrace();
        }
    }

}
