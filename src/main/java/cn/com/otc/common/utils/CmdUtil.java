package cn.com.otc.common.utils;

import cn.hutool.json.JSONUtil;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class CmdUtil {
	
	/**
	 * cmd 命令执行
	 */
	public static Map<String, Object> cmdCommand(String cmdKey, String cmdPam){
		Map<String, Object> map = new HashMap<String, Object>();
		StringBuffer cmd = new StringBuffer();
		cmd.append("sh "+cmdKey);
		if(StringUtils.isNotBlank(cmdPam)){
			cmd.append(" ");
			cmd.append(cmdPam);
		}
		StringBuffer resultStringBuffer = new StringBuffer();
		String lineToRead = "";
		try {
			Process proc = Runtime.getRuntime().exec(cmd.toString());
			InputStream inputStream = proc.getInputStream();
			BufferedReader bufferedRreader = new BufferedReader(
					new InputStreamReader(inputStream));
			while ((lineToRead = bufferedRreader.readLine()) != null) {
				resultStringBuffer.append(lineToRead);
			}
			proc.waitFor();
			map.put("code", "200");
			map.put("htmlWebPage", resultStringBuffer.toString());
		} catch (Exception e) {
			e.printStackTrace();
			map.put("code", "201");
			map.put("htmlWebPage", e.getMessage());
		}
		return map;
	}
	
	public static Map<String, Object> procCmd(String command){
		Map<String, Object> map = new HashMap<String, Object>();
		StringBuffer resultStringBuffer = new StringBuffer();
		String lineToRead = "";
		try {
			   String os = System.getProperty("os.name");
			   if(os.toLowerCase().startsWith("win")){
            	String[] procCmd = { "cmd", "/c", command};
            	Process proc = Runtime.getRuntime().exec(procCmd);
							InputStream inputStream = proc.getInputStream();
							BufferedReader bufferedRreader = new BufferedReader(
									new InputStreamReader(inputStream,"gbk"));
							while ((lineToRead = bufferedRreader.readLine()) != null) {
								resultStringBuffer.append(lineToRead).append(",");
							}
							if (resultStringBuffer.length() > 1) {
								resultStringBuffer.setLength(resultStringBuffer.length() - 1);
							}
							StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "Error");
							StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "Output");
							errorGobbler.start();
							outputGobbler.start();
							proc.waitFor();
         }else{
            	String[] procCmd = { "/bin/sh", "-c", command};
            	Process proc = Runtime.getRuntime().exec(procCmd);
							InputStream inputStream = proc.getInputStream();
							BufferedReader bufferedRreader = new BufferedReader(
									new InputStreamReader(inputStream,"UTF-8"));
							while ((lineToRead = bufferedRreader.readLine()) != null) {
								resultStringBuffer.append(lineToRead).append(",");
							}
							if (resultStringBuffer.length() > 1) {
								resultStringBuffer.setLength(resultStringBuffer.length() - 1);
							}
							StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "Error");
							StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "Output");
							errorGobbler.start();
							outputGobbler.start();
							proc.waitFor();
			   }
				 map.put("code", "200");
				 map.put("htmlWebPage", resultStringBuffer.toString());
		} catch (Exception e) {
			log.error(String.format("执行python 命令失败,command={%s},具体失败信息:",command),e);
			map.put("code", "201");
			map.put("htmlWebPage", e.getMessage());
		}
		return map;
	}

	public static void main(String[] args) {
		Map<String, Object> result  = procCmd("cd D:\\idea\\github\\antwalletbot-py && python3 antwalletbot_tonutils.py -g gettxstatus -tid d841ab4e94d6b07e4c50ace8a8a95282880111701ff828a63616d58312bbd839");
		System.out.println(JSONUtil.toJsonStr(result));
	}

}
