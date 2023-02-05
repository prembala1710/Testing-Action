import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
public class CliqInformer {
	public static void main(String args[]) {
		System.out.println("Calling Cliq...");
		HttpURLConnection connection;
		Integer MAX_MESSAGE_LENGTH = 4096;
		String Message_BREAK = "\\\n";
		Integer status = 400;
		StringBuffer responseContent = new StringBuffer();
		try {
      boolean error = false;
			String message;
			String CustomMessage;
			String CliqWebhookToken = args[0];
			String CliqChannelLink = args[1];
			if(!CliqChannelLink.contains("https://cliq.zoho.com/api/v2/channelsbyname/") || !CliqChannelLink.contains("message"))
        if(CliqChannelLink.matches("[a-z]+"))
          CliqChannelLink = "https://cliq.zoho.com/api/v2/channelsbyname/" + CliqChannelLink + "/message";			  
			String Event = args[2];
			String[] EventWords = Event.split("_");
			Event = new String();
			for(String s: EventWords)
			  Event += s.substring(0,1).toUpperCase() + s.substring(1) + " ";
			Event = Event.trim();
			String Action = args[3];
			if(!Action.equals(""))
			{
			  String[] ActionWords = Action.split("_");
			  Action = new String();
			  for(String s: ActionWords)
			    Action += s + " ";
			  Action = Action.trim();
			}
			String ServerURL = args[4];
			String Repository = args[5];
			String Workflow = args[6];
			String Actor = args[7];
			String RunId = args[8];
			String Ref = args[9];
			String ActorURL = ServerURL + "/" + Actor;
			String RepositoryURL = ServerURL + "/" + Repository;
			String WorkflowURL = RepositoryURL + "/actions/runs/" + RunId;
			String RefURL = RepositoryURL;
			if(Ref.contains("pull"))
			  RefURL = RefURL + "/pull/";
			else
			  RefURL = RefURL + "/tree/";
			System.out.println(Ref);
			if(Ref.split("/").length > 2)
			  Ref = Ref.split("/")[2];
		  System.out.println(Ref);
			RefURL = RefURL + Ref;
			String CliqInformerURL = "https://workdrive.zohoexternal.com/external/047d96f793983933bbdb59deb9c44f5443b83a7188e278736405d4d733923181/download?directDownload=true";
			if(Action.equals(""))
        Action = "made";
			CustomMessage = args[10];
			message = CustomMessage;
			message = message.replace("(me)","[" + Actor + "](" + ActorURL + ")");
			message = message.replace("(workflow)","[" + Workflow + "](" + WorkflowURL + ")" );
			message = message.replace("(repo)","[" + Repository + "](" + RepositoryURL + ")" );
			message = message.replace("(event)","*" + Event + "*");
			message = message.replace("(action)",Action);
			message = message.replace("(ref)","[" + Ref + "](" + RefURL + ")" );
			if(message.length() > 4096)
			{
			  message = CustomMessage;
			  message = message.replace("(me)","*" + Actor + "*");
			  message = message.replace("(workflow)","*" + Workflow + "*" );
			  message = message.replace("(repo)","*" + Repository + "*" );
			  message = message.replace("(event)","*" + Event + "*");
			  message = message.replace("(action)",Action);
			  message = message.replace("(ref)","*" + Ref + "*" );
			}
			ArrayList<String> messages = new ArrayList<String>();
			for(int i = 0 ; i < message.length() ;)
			{
			  String split_message;
			  if(i+MAX_MESSAGE_LENGTH < message.length())
			  {
			    split_message = message.substring(i,i+MAX_MESSAGE_LENGTH);
			    int displaced_length = MAX_MESSAGE_LENGTH;
			    if(split_message.contains("\\\n"))
			    {
			      displaced_length = split_message.lastIndexOf("\\\n") + 2;
			      split_message = message.substring(i,i+displaced_length);
			      split_message = split_message.replaceAll("\\\\\n","");
			    }
			    else if(split_message.contains("\n"))
			    {
			      displaced_length = split_message.lastIndexOf("\n") + 1;
			      split_message = message.substring(i,i+displaced_length);
			    }
			    else if(split_message.contains("."))
			    {
			      displaced_length = split_message.lastIndexOf(".") + 1;
			      split_message = message.substring(i,i+displaced_length);
			    }
			    i += displaced_length;
			  }
			  else
			  {
			    split_message = message.substring(i,message.length());
			    i+= MAX_MESSAGE_LENGTH;
			  }
			  messages.add(split_message);
			}
			for(String msg : messages)
			{
			  msg = msg.replace("\"","'");
			  String TextParams = "{\n\"text\":\"" + msg + "\",\n\"bot\":\n{\n\"name\":\"CliqInformer\",\n\"image\":\"" + CliqInformerURL + "\"\n}}\n";
			  connection = (HttpURLConnection) new URL(CliqChannelLink + "?zapikey=" + CliqWebhookToken).openConnection();
			  connection.setRequestMethod("POST");
			  connection.setRequestProperty("Content-Type","application/json");
			  connection.setDoOutput(true);
			  OutputStream os = connection.getOutputStream();
			  os.write(TextParams.getBytes());
			  os.flush();
			  os.close();
			  status = connection.getResponseCode();
			  if(status > 299) {
				  BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
				  String line;
				  while((line = reader.readLine()) != null) {
					  responseContent.append(line);
				  }
			    reader.close();
			  }
			  else
			  {
				  BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				  String line;
				  while((line = reader.readLine()) != null) {
					  responseContent.append(line);
				  }
				  reader.close();
			  }
			  System.out.println(responseContent);
			}
			var githubOutput = System.getenv("GITHUB_OUTPUT");
			if(status == 204)
			{
			  if(githubOutput == null)
			    error = true;
			}
			else
			{
			  error = true;
			}
			Integer value = 400;
	    if(!error)
	      value = 204;
			var file = Path.of(githubOutput);
			if(file.getParent() != null) Files.createDirectories(file.getParent());
			var lines = ("message-status=" + value).lines().toList();
			if(lines.size() != 1)
			  error = true;
			Files.write(file, lines, UTF_8 , CREATE , APPEND , WRITE);
			System.out.println("Message - Status : " + value);
		}  catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
