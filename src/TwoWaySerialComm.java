import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

public class TwoWaySerialComm {
    public TwoWaySerialComm() {
        super();
    }

    private static SerialPort serialPort;

    void connect(String portName) {
        /**
         * 方式1
         */
        /*
        Enumeration ports = CommPortIdentifier.getPortIdentifiers();
        while (ports.hasMoreElements()) {
            CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();
            System.out.println(port.getName() + " -> " + port.getCurrentOwner());
            switch (port.getPortType()) {
                case CommPortIdentifier.PORT_PARALLEL:
                    System.out.println("parell");
                    break;
                case CommPortIdentifier.PORT_SERIAL:
                    try {
                        // 1 custom name  2 open timeout time
                        serialPort = (SerialPort) port.open("core", 1000);
                        int baudRate = 115200; // 115200bps
                        serialPort.setSerialPortParams(
                                baudRate,
                                SerialPort.DATABITS_8,
                                SerialPort.STOPBITS_1,
                                SerialPort.PARITY_NONE);
                    } catch (PortInUseException e) {
                        System.out.println(e.getMessage());
                    } catch (UnsupportedCommOperationException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
            }

            try {
                InputStream in = serialPort.getInputStream();
                new Thread(new SerialReader(in)).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        */

        /**
         * 方式2
         */
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            if (portIdentifier.isCurrentlyOwned()) {
                System.out.println("Error: Port is currently in use");
            } else {
                // 打开端口，（自定义名字，打开超时时间）
                CommPort commPort = portIdentifier.open("custom serial", 2000);

                if (commPort instanceof SerialPort) {
                    serialPort = (SerialPort) commPort;
                    // 设置串口参数（波特率，数据位8，停止位1，校验位无）
                    serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                    InputStream in = serialPort.getInputStream();
                    OutputStream out = serialPort.getOutputStream();

                    new Thread(new SerialReader(in)).start();
                    new Thread(new SerialWriter(out)).start();

                } else {
                    System.out.println("Error: Only serial ports are handled by this example.");
                }
            }
        } catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException | IOException e) {
            e.printStackTrace();
        }

    }

    public static class SerialReader implements Runnable {
        InputStream in;

        public SerialReader(InputStream in) {
            this.in = in;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int len = -1;
            try {
                while ((len = this.in.read(buffer)) > -1) {
                    byte[] ret = new byte[len];
                    System.arraycopy(buffer, 0, ret, 0, len);
                    System.out.println(ByteUtil.byte2hex(ret));
//                    System.out.print(new String(buffer, 0, len));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeSerialPort(SerialPort serialPort) {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
            System.out.println("The serial port is closed:" + serialPort.getName());
            serialPort = null;
        }
    }


    public static class SerialWriter implements Runnable {
        OutputStream out;

        public SerialWriter(OutputStream out) {
            this.out = out;
        }

        public void run() {
            try {
                int c = 0;
                while ((c = System.in.read()) > -1) {
                    this.out.write(c);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setListenerToSerialPort(SerialPort serialPort, SerialPortEventListener listener) {
        try {
            serialPort.addEventListener(listener);
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }
        serialPort.notifyOnDataAvailable(true); //串口有数据监听
        serialPort.notifyOnBreakInterrupt(true); //中断事件监听
    }


    public static void main(String[] args) {
        try {
            new TwoWaySerialComm().connect("COM39");

            setListenerToSerialPort(serialPort, new SerialPortEventListener() {
                @Override
                public void serialEvent(SerialPortEvent arg0) {
                    if (arg0.getEventType() == SerialPortEvent.DATA_AVAILABLE) { //数据通知
                        System.out.println("data available listener");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}