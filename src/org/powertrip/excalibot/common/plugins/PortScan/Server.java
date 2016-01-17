package org.powertrip.excalibot.common.plugins.PortScan;

import org.powertrip.excalibot.common.com.*;
import org.powertrip.excalibot.common.plugins.ArthurPlug;
import org.powertrip.excalibot.common.plugins.interfaces.arthur.KnightManagerInterface;
import org.powertrip.excalibot.common.plugins.interfaces.arthur.TaskManagerInterface;
import org.powertrip.excalibot.common.utils.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * Created by Tiago on 04/01/2016.
 * 04:11
 */
public class Server extends ArthurPlug{
	public Server(KnightManagerInterface knightManager, TaskManagerInterface taskManager) {
		super(knightManager, taskManager);
	}
	String Timeend="500";

	@Override
	public PluginHelp help() {
		return new PluginHelp().setHelp("::PortScan [Port Scanning] Usage: PortScan address:<address> bots:<bots (bots%2=0)>");
	}

	@Override
	public TaskResult check(Task task) {
		TaskResult result = new TaskResult();

		Long total = taskManager.getKnightCount(task.getTaskId());
		Long recev = taskManager.getResultCount(task.getTaskId());

		result
			.setSuccessful(true)
			.setTaskId(task.getTaskId())
			.setResponse("total", total.toString())
			.setResponse("done", recev.toString())
			.setComplete(total.equals(recev));
		return result;
	}

	@Override
	public TaskResult get(Task task) {
		Long total = taskManager.getKnightCount(task.getTaskId());
		Long recev = taskManager.getResultCount(task.getTaskId());

		TaskResult result = new TaskResult()
									.setTaskId(task.getTaskId())
									.setSuccessful(true)
									.setComplete(total.equals(recev));


		List<String> data = taskManager.getAllResults(task.getTaskId()).stream()
				.filter(subTaskResult -> subTaskResult.getResponseMap().containsKey("Ports"))
				.map(subTaskResult1 -> subTaskResult1.getResponse("Ports"))
				.collect(Collectors.toList());


		return result.setResponse("stdout", "Ports open: " + data.get(0));
	}

	@Override
	public void handleSubTaskResult(Task task, SubTaskResult subTaskResult) {
		/**
		 * Only if I need to do anything when I get a reply.
		 */
	}

	@Override
	public TaskResult submit(Task task) {
		//Get my parameter map, could use task.getParameter(String key), but this is shorter.
		Logger.log(task.toString());
		Map args = task.getParametersMap();

		//Declare my parameters
		String address;
		long botCount;

		//Create a TaskResult and fill the common fields.
		TaskResult result = new TaskResult()
									.setTaskId(task.getTaskId())
									.setSuccessful(false)
									.setComplete(true);

		//No Dice! Wrong parameters.
		if( !args.containsKey("address") || !args.containsKey("bots") ) {
			return result.setResponse("stdout", "Wrong parameters");
		}
		else if (Long.parseLong((String) args.get("bots"))%2!=0)
			return result.setResponse("stdout", "Please insert bots number where bots%2=0");

		//Parse parameters
		address = (String) args.get("address");
		botCount = Long.parseLong((String) args.get("bots"));

        //Split port's numbers by bot
		int i=0;
		int scannernumbers= (int) botCount;
		int space = 65536 / scannernumbers;
		int[] init = new int[scannernumbers];
		int[] end = new int[scannernumbers];
		for (i = 0; i < scannernumbers; i++) {
			init[i] = space * i;
			end[i] = init[i] + space;
		}
		i=0;
		try {
			//Get bots alive in the last 50 seconds and get as many as needed
			List<KnightInfo> bots = knightManager.getFreeKnightList(50000).subList(0, (int) botCount);
			for(KnightInfo bot : bots){
				knightManager.dispatchToKnight(
						new SubTask(task, bot)
								.setParameter("address", address)
                                .setParameter("init",Integer.toString(init[i]))
                                .setParameter("end",Integer.toString(end[i]))
                                .setParameter("Timeout",Timeend)
				);
                i++;
			}
			result
				.setSuccessful(true)
				.setResponse("stdout", "Task accepted, keep an eye out for the results :D");
		}catch (IndexOutOfBoundsException e) {
			//No bots...
			result.setResponse("stdout", "Not enough free bots.");
		}
		return result;
	}
}
