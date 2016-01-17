package org.powertrip.excalibot.common.plugins.PortScan;

import org.powertrip.excalibot.common.com.SubTask;
import org.powertrip.excalibot.common.com.SubTaskResult;
import org.powertrip.excalibot.common.plugins.KnightPlug;
import org.powertrip.excalibot.common.plugins.interfaces.knight.ResultManagerInterface;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * Created by Tiago on 04/01/2016.
 * 04:12
 */
public class Bot extends KnightPlug{
	public Bot(ResultManagerInterface resultManager) {
		super(resultManager);
	}

	@Override
	public boolean run(SubTask subTask) {
        String openPorts="";
		SubTaskResult result = subTask.createResult();
		String address = subTask.getParameter("address");
		int init= Integer.parseInt(subTask.getParameter("init"));
		int end= Integer.parseInt(subTask.getParameter("end"));
		int timeout= Integer.parseInt(subTask.getParameter("Timeout"));
        System.out.println("Timeout: "+timeout);
		DatagramSocket socket = null;
        int k=0;
		byte[] data = address.getBytes();
        for(int i=init; i<end;i++) {
            try {
                socket = new DatagramSocket();
                socket.setSoTimeout(timeout);
                socket.setTrafficClass(0x04 | 0x10);
                socket.connect(new InetSocketAddress(address, i));
                socket.send(new DatagramPacket(data, data.length));
                System.out.println(data.length+" "+address+" "+i);
                while (true) {
                    byte[] receive = new byte[4096];
                    DatagramPacket response = new DatagramPacket(receive, 4096);
                    socket.receive(response);
                    if (response != null || response.getData() != null) {
                        k++;
                        openPorts.concat(Integer.toString(i) + " ");
                        break;
                    }
                    System.out.println(openPorts);
                }
            } catch (Exception e) {
                System.err.println("Problem on connection.");//e.printStackTrace();
            } finally {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (Exception e) {
                    System.err.println("Problem on connection.");
                }
            }
        }
		try {
            if(k>=end-init-1)
                result
                        .setSuccessful(true)
                        .setResponse("Ports", "All ports open");
            else if(k>0)
                result
                        .setSuccessful(true)
                        .setResponse("Ports", openPorts);
            else
                result
                        .setSuccessful(false)
                        .setResponse("Ports", "All ports closed");
			resultManager.returnResult(result);
			return result.isSuccessful();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}
}
