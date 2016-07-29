package de.fhg.fokus.ims;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.ims.Configuration;
import javax.ims.ImsException;
import javax.ims.ServiceClosedException;
import javax.ims.core.Capabilities;
import javax.ims.core.CoreService;
import javax.ims.core.Message;
import javax.ims.core.MessageBodyPart;
import javax.ims.core.PageMessage;
import javax.ims.core.PageMessageListener;
import javax.ims.core.Publication;
import javax.ims.core.PublicationListener;
import javax.ims.core.Reference;
import javax.ims.core.ReferenceListener;
import javax.ims.core.Session;
import javax.ims.core.Subscription;
import javax.ims.core.media.FramedMedia;
import javax.ims.core.media.FramedMediaListener;
import javax.ims.core.media.Media;
import javax.ims.core.media.StreamMedia;

import org.apache.log4j.ConsoleAppender;

import org.apache.log4j.PatternLayout;

import de.fhg.fokus.ims.core.CoreServiceImpl;
import de.fhg.fokus.ims.core.CoreServiceListener2;
import de.fhg.fokus.ims.core.IMSConfigurator;
import de.fhg.fokus.ims.core.IMSManager;
import de.fhg.fokus.ims.core.IMSProfile;
import de.fhg.fokus.ims.core.Session2;
import de.fhg.fokus.ims.core.SessionListener2;
import de.fhg.fokus.ims.core.Subscription2;
import de.fhg.fokus.ims.core.SubscriptionImpl;
import de.fhg.fokus.ims.core.SubscriptionListener2;
import de.fhg.fokus.ims.core.net.NetworkService;
import de.fhg.fokus.ims.core.utils.SIPUtils;
import de.fhg.fokus.ims.core.utils.Utils;
import de.fhg.fokus.microedition.io.Connector;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleUE implements CoreServiceListener2, PageMessageListener, SubscriptionListener2, SessionListener2, FramedMediaListener, PublicationListener
{
	BufferedReader in;
	protected CoreServiceImpl coreservice;
	protected IMSProfile imsProfile;
	protected Vector sessions = new Vector();
	private MemoryAppender appender = new MemoryAppender();
	private Command unknownCommand = new UnknownCommand();
	private ProfileService profileService = new ProfileService();
	private NetworkService networkService = new NetworkService();	

	protected Command[] commands = new Command[]
	{ new PrintHelpCommand(), new ExitCommand(), new RegisterCommand(), new UnregisterCommand(), new PublishCommand(), new UnpublishCommand(),
			new ListSubscriptions(), new SubscribeCommand(), new UnsubscribeCommand(), new MessageCommand(), new ListCallsCommand(), new MakeCallCommand(),
			new AbortCommand(), new AnswerCommand(), new RejectCommand(), new HangupCommand(), new HoldCommand(), new ResumeCommand(), new StartChatCommand(),new UpdateChatCommand(),
			new AddChatCommand(), new RemoveChatCommand(), new SayCommand(), new DumpLogCommand(appender), new RefersToCommand(), new AddStreamMedia(), new RemoveStreamMedia() };
	private Subscription2 subscription2;

	public ConsoleUE()
	{
		initLogger();


		UserProfile profile = profileService.getProfile();		
		initConfiguration(profile);

		in = new BufferedReader(new InputStreamReader(System.in));
	}

	public void addCommand(Command command)
	{
		Command[] temp = new Command[commands.length + 1];
		System.arraycopy(commands, 0, temp, 0, commands.length);
		temp[temp.length - 1] = command;
		commands = temp;
	}

	private void initLogger()
	{
		appender.setLayout(new PatternLayout("%r [%t] %p %c %x - %m%n"));

		ConsoleAppender console = new ConsoleAppender(new PatternLayout("%r [%t] %p %c %x - %m%n"));
		org.apache.log4j.Logger.getRootLogger().addAppender(console);
		org.apache.log4j.Logger.getRootLogger().addAppender(appender);

		/*
		 * File f = new File("log4j.properties"); if (f.exists()) PropertyConfigurator.configure(f.getPath());
		 */
		
		BasicConfigurator.configure();
		org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
		rootLogger.setLevel(Level.ALL);
		
		org.apache.log4j.Logger.getLogger("org.apache.commons").setLevel(Level.ALL);
		org.apache.log4j.Logger.getLogger("httpclient").setLevel(Level.ALL);
		
		Logger logger = LoggerFactory.getLogger(ConsoleUE.class);
		logger.debug("Hello MONSTER");
	}

	private boolean initConfiguration(UserProfile userProfile)
	{
		try
		{
			this.imsProfile = new IMSProfile(userProfile);
			IMSConfigurator configurator = new IMSConfigurator(this.imsProfile);

			configurator.configure(IMSManager.getInstance(), networkService);
			//StreamMediaFactoryBase factory = (StreamMediaFactoryBase) Class.forName("de.fhg.fokus.ims.core.media.StreamMediaFactory").newInstance();
			
			//factory.getClass().getMethod("serviceInject", new Class[]
			//{ ProfileService.class }).invoke(factory, new Object[]
			//{ profileService });

			//IMSManager.getInstance().setStreamMediaFactory(factory);

			IMSManager.getInstance().start();

			String[][] omaIMServiceRegistry = new String[][]
			                                       		{
			                                       		{ "CoreService", "nubomedia", "", "", "+g.3gpp.cs-voice" }
//														{ "CoreService", "monster", "", "", "" }
			                                       		};
			
			Configuration.getConfiguration().setRegistry("nubomedia", "", omaIMServiceRegistry);
			
			this.coreservice = (CoreServiceImpl) Connector.open("imscore://nubomedia;serviceId=nubomedia");
			this.coreservice.setListener(this);
			return true;
		} catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("Error: " + e.getMessage());

			return false;
		}
	}

	private String getLocalIP(String remoteIP, int remotePort) throws IOException
	{
		Socket socket = null;
		try
		{
			socket = new Socket(remoteIP, remotePort);
			remoteIP = socket.getLocalAddress().toString();

			if (remoteIP.startsWith("/"))
				remoteIP = remoteIP.substring(1);
			return remoteIP;
		} catch (UnknownHostException e)
		{
			e.printStackTrace();
			return null;
		} catch (IOException e)
		{
			e.printStackTrace();
			return null;
		} finally
		{
			if (socket != null)
			{
				socket.close();
			}
		}
	}

	private Command getConsoleCommand(List params)
	{
		String command = null;
		do
		{
			System.out.print("IMS CONNECTOR-Console> ");
			try
			{
				command = in.readLine();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			if (command == null)
				return unknownCommand;
		} while (command.length() == 0);
		StringTokenizer st = new StringTokenizer(command, " ");
		if (!st.hasMoreTokens())
			return unknownCommand;

		String cmdName = st.nextToken();
		Command result = null;
		for (int i = 0; i < commands.length; i++)
		{
			for (int j = 0; j < commands[i].name.length; j++)
			{
				if (commands[i].name[j].equals(cmdName))
				{
					result = commands[i];
					break;
				}
			}
			if (result != null)
				break;
		}

		if (result == null)
			return unknownCommand;
		while (st.hasMoreTokens())
			params.add(st.nextToken());

		return result;
	}

	public void start()
	{
		Command cmd;
		ArrayList params = new ArrayList();

		boolean running = true;

		while (running)
		{
			params.clear();
			cmd = getConsoleCommand(params);

			try
			{
				cmd.execute((String[]) params.toArray(new String[params.size()]));
			} catch (Exception e)
			{
				e.printStackTrace();
			}

			if (cmd instanceof ExitCommand)
				running = false;
		}

		System.out.println("... Bye Bye!");
		System.exit(0);
	}

	protected String toUserURI(String x)
	{
		if (x.substring(0, 1).equals("+"))
			x = "tel:" + x;
		else if (!x.substring(0, 3).equalsIgnoreCase("sip:") && !x.substring(0, 3).equalsIgnoreCase("tel:"))
			x = "sip:" + x;
		if (x.indexOf('@') < 0 && x.indexOf('.') < 0 && x.indexOf("tel:") < 0)
			x = x + "@" + imsProfile.getDomain();
		return x;
	}

	protected Integer toIntParam(String x)
	{
		try
		{
			Integer k = Integer.valueOf(x);
			return k;
		} catch (NumberFormatException e)
		{
			return null;
		}
	}

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws Exception
	{
		System.out.println("--- Console interface for the Fraunhofer FOKUS/NGNI MONSTER ---");
		System.out.println("   Authors:");
		System.out.println("      Alice Cheambe    <alice.cheambe@fokus.fraunhofer.de>");
		System.out.println("      Dragos Vingarzan <vingarzan@fokus.fraunhofer.de>");
		System.out.println("      Andreas Bachmann <andreas.bachmann@fokus.fraunhofer.de>\n");
		
		ConsoleUE cui = new ConsoleUE();
		cui.start();
		System.exit(0);
	}

	// ------ Event handler ------//

	// --- Messaging --- //
	public void pageMessageReceived(CoreService coreservice, PageMessage pagemessage)
	{
		System.out.print("\nMessage ");
		System.out.print("From: " + pagemessage.getRemoteUserId());
		System.out.println("ContentType: " + pagemessage.getContentType());
		System.out.println(new String(pagemessage.getContent()));
	}

	public void pageMessageDelivered(PageMessage pagemessage)
	{
		System.out.print("the pagemessage has been successfully delivered");
	}

	public void pageMessageDeliveredFailed(PageMessage pagemessage)
	{
		System.out.print("the pagemessage was NOT delivered");
	}

	// --- Refer --- //
	public void referRequestReceived(CoreService coreservice, Reference reference)
	{
		System.out.println("Received reference: " + reference.getReferMethod() + " " + reference.getReferToUserId());
		try
		{
			reference.reject();
		} catch (ServiceClosedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void serviceClosed(CoreService service)
	{
		// TODO Auto-generated method stub
	}

	public void sessionInvitationReceived(CoreService coreservice, Session session)
	{
		session.setListener(this);
		sessions.add(session);
		System.out.println("Session received");
	}

	public void unsolicitedNotifyReceived(CoreService coreservice, String s)
	{
		// TODO Auto-generated method stub
	}

	public void subscriptionReceived(CoreService coreservice, Subscription subscription)
	{
		System.out.print("Incoming subscribe message");				
		
		
	}

	public void subscriptionNotify(Subscription subscription, Message notify)
	{
		System.out.print("Incoming Notify message");
		MessageBodyPart parts = notify.getBodyParts()[0];
		System.out.print("content type is " + parts.getHeader("Content-Type"));
		try
		{
			System.out.print(Utils.ConvertByteToString((ByteArrayInputStream) parts.openContentInputStream()));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void subscriptionStartFailed(Subscription subscription)
	{
		System.out.print("Subscription start failed - " + subscription.getRemoteUserId());
	}

	public void subscriptionStarted(Subscription subscription)
	{
		System.out.print("Subscription started - " + subscription.getRemoteUserId());
	}

	public void subscriptionTerminated(Subscription subscription)
	{
		System.out.print("Subscription terminated - " + subscription.getRemoteUserId());
	}

	public void publicationTerminated(Publication publication)
	{
		Message[] m = publication.getPreviousResponses(Message.PUBLICATION_UNPUBLISH);
		System.out.println("Publication devlivered: " + m[0].getStatusCode() + " - " + m[0].getReasonPhrase());
	}

	public void publishDelivered(Publication publication)
	{
		Message[] m = publication.getPreviousResponses(Message.PUBLICATION_PUBLISH);
		System.out.println("Publication devlivered: " + m[0].getStatusCode() + " - " + m[0].getReasonPhrase());
	}

	public void publishDeliveredFailed(Publication publication)
	{
		Message[] m = publication.getPreviousResponses(Message.PUBLICATION_PUBLISH);
		System.out.println("Publication devlivered: " + m[0].getStatusCode() + " - " + m[0].getReasonPhrase());
	}

	public void sessionAlerting(Session session)
	{
		System.out.println("session alerting");
	}

	public void sessionReferenceReceived(Session session, Reference reference)
	{
		System.out.println("session reference received");
		try
		{
			System.out.println("session accepting refer");
			reference.accept();
			
			String uri = SIPUtils.getSipUri(reference.getReferToUserId());
			
			Session newSession = coreservice.createSession(null, uri);
			newSession.setListener(ConsoleUE.this);
			StreamMedia myMedia = (StreamMedia) newSession.createMedia("StreamMedia", Media.DIRECTION_SEND_RECEIVE);
			myMedia.setStreamType(StreamMedia.STREAM_TYPE_AUDIO);
			myMedia.setSource("capture://audio");
			sessions.add(newSession);
			
			reference.connectReferMethod(newSession);
			
			newSession.start();
		
		} catch (ServiceClosedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ImsException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void sessionSubscriptionReceived(Session session, Subscription subscription)
	{
		
	}

	public void sessionStartFailed(Session session)
	{
		Message msg = session.getPreviousResponses(Message.SESSION_START)[0];
		System.out.println("session start failed: status code:" + msg.getStatusCode() + " reason:" + msg.getReasonPhrase());

		sessions.remove(session);
	}

	public void sessionStarted(Session session)
	{
		System.out.println("session started");

		Media[] media = session.getMedia();
		for (int i = 0; i < media.length; i++)
		{
			if (media[i] instanceof FramedMedia)
				((FramedMedia) media[i]).setListener(this);
		}
	}

	public void sessionTerminated(Session session)
	{
		Message msg = session.getPreviousResponses(Message.SESSION_TERMINATE)[0];
		System.out.println("session terminated: status code:" + msg.getStatusCode() + " reason:" + msg.getReasonPhrase());
		sessions.remove(session);
	}

	public void sessionUpdateFailed(Session session)
	{
		Message msg = session.getPreviousResponses(Message.SESSION_START)[0];
		System.out.println("session update failed: status code:" + msg.getStatusCode() + " reason:" + msg.getReasonPhrase());
	}

	public void sessionUpdateReceived(Session session)
	{
		System.out.println("session update received");
	}

	public void sessionUpdated(Session session)
	{
		System.out.println("session updated");
	}

	// --- Chat ---//
	public void connectionError(FramedMedia media)
	{
		// TODO Auto-generated method stub
	}

	public void contentReceiveFailed(FramedMedia media, String messageId)
	{
		// TODO Auto-generated method stub
	}

	public void contentReceived(FramedMedia media, String messageId, int size, String fileName)
	{
		System.out.println("UE: Content Received...");
		byte[] message;
		try
		{
			message = media.receiveBytes(messageId);
			System.out.println(new String(message, 0, size, "utf-8"));
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void deliveryFailure(FramedMedia media, String messageId, int statusCode, String reasonPhrase)
	{
		// TODO Auto-generated method stub

	}

	public void deliverySuccess(FramedMedia media, String messageId)
	{
		System.out.println("message sent");
	}

	public void transferProgress(FramedMedia media, String messageId, int bytesTransferred, int bytesTotal)
	{
		// TODO Auto-generated method stub

	}

	// ------ Command classes ------//

	public abstract class Command
	{
		private String[] name;
		private String help;

		public Command(String[] name, String help)
		{
			this.name = name;
			this.help = help;
		}

		public abstract void execute(String[] params);

		public String toString()
		{
			int j;
			StringBuffer stb = new StringBuffer();
			stb.append(name[0]);
			stb.append("\t");
			stb.append(help);
			if (name.length > 1)
			{
				stb.append(" (alias: ");
				for (j = 1; j < name.length; j++)
				{
					if (j != 1)
						stb.append(",");
					stb.append(name[j]);
				}
				stb.append(")");
			}
			return stb.toString();
		}
	}

	private class ExitCommand extends Command
	{
		public ExitCommand()
		{
			super(new String[]
			{ "x", "exit" }, "               - Exit the application");
		}

		public void execute(String[] params)
		{
			if (coreservice != null)
			{
				System.out.println("Performing nice exit...");
				coreservice.close();
			}
		}
	}

	private class DumpLogCommand extends Command
	{
		private MemoryAppender appender;

		public DumpLogCommand(MemoryAppender appender)
		{
			super(new String[]
			{ "dl", "dump" }, "               - Dumps the log");
			this.appender = appender;
		}

		public void execute(String[] params)
		{
			String[] logs = appender.getLogs();
			int count = logs.length;
			if (params.length > 0)
			{
				int x = Integer.parseInt(params[0]);
				count = Math.min(count, x);
			}
			for (int i = (logs.length - count); i < logs.length; i++)
			{
				System.out.print(logs[i]);
			}
		}
	}

	private class PrintHelpCommand extends Command
	{
		public PrintHelpCommand()
		{
			super(new String[]
			{ "help" }, "prints this screens");
		}

		public void execute(String[] params)
		{
			System.out.println("Console commands:");
			for (int i = 1; i < commands.length; i++)
			{
				System.out.println("  " + commands[i].toString());
			}
		}
	}

	private class UnknownCommand extends Command
	{
		public UnknownCommand()
		{
			super(null, null);
		}

		public void execute(String[] params)
		{
			System.out.println("Command unknown, please use one of the following");
			System.out.println();
			System.out.println("Console commands:");
			for (int i = 0; i < commands.length; i++)
			{
				System.out.println("  " + commands[i].toString());
			}
		}
	}

	private class RegisterCommand extends Command
	{
		public RegisterCommand()
		{
			super(new String[]
			{ "r", "reg", "register" }, "<expires>      - Register the User endpoint");
		}

		public void execute(String[] params)
		{
			try
			{
				coreservice = (CoreServiceImpl) Connector.open("imscore://monster");
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private class UnregisterCommand extends Command
	{
		public UnregisterCommand()
		{
			super(new String[]
			{ "ur", "ureg", "unreg", "unregister" }, "               - Unregister the User endpoint");
		}

		public void execute(String[] params)
		{
			if (coreservice != null)
			{
				coreservice.close();
				coreservice = null;
			}
		}
	}

	private class SubscribeCommand extends Command
	{
		public SubscribeCommand()
		{
			super(new String[]
			{ "s", "subs", "subscribe" }, "<uri> [<exp>]  - Subscribe to presence information of <uri>");
		}

		public void execute(String[] params)
		{
			// int expires = 600;
			if (params.length < 1)
			{
				System.out.println("Parameter count wrong, expected 1 found " + params.length);
				return;
			} else if (params.length == 2)
			{
				// expires = Integer.valueOf(params[1]).intValue();
				try
				{

					Subscription sub = ((Session2)sessions.get(0)).createSubscription("conference");
					sub.setListener(ConsoleUE.this);
					sub.subscribe();

				} catch (IllegalStateException e2)
				{
					e2.printStackTrace();
				} catch (ServiceClosedException e2)
				{
					e2.printStackTrace();
				} catch (ImsException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	private class UnsubscribeCommand extends Command
	{
		public UnsubscribeCommand()
		{
			super(new String[]
			{ "us", "usubs", "unsubs", "unsubscribe" }, "<uri>          - UnSubscribe to presence information of <uri>");
		}

		public void execute(String[] params)
		{
			if (params.length != 1)
			{
				System.out.println("Parameter count wrong: \n Only Call-ID needed.");
				return;
			}
			try
			{
//				XXX:fsc
//				SubscriptionImpl sub = coreservice.getSubscriptions().get(toUserURI(params[0]), "presence");
				SubscriptionImpl sub = coreservice.getSubscriptions().get(toUserURI(params[0]));
				sub.unsubscribe();
			} catch (IllegalStateException e2)
			{
				e2.printStackTrace();
			} catch (ServiceClosedException e2)
			{
				e2.printStackTrace();
			}
		}
	}

	private class PublishCommand extends Command
	{
		public PublishCommand()
		{
			super(new String[]
			{ "p", "pub", "publish" }, "<stat> [<exp>] - Publish own presence information");
		}

		public void execute(String[] params)
		{

			StringBuffer onlineState = new StringBuffer();
			onlineState
					.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><presence  xmlns=\"urn:ietf:params:xml:ns:pidf\" xmlns:dm=\"urn:ietf:params:xml:ns:pidf:data-model\" xmlns:rpid=\"urn:ietf:params:xml:ns:pidf:rpid\" xmlns:op=\"urn:oma:params:xml:ns:pidf:oma-pres\" entity=\"sip:blum@open-ims.org\">");
			onlineState.append("<tuple id=\"t4\">");
			onlineState.append("<status><basic>open</basic>");
			onlineState.append("</status><op:willingness><op:basic>open</op:basic>");
			onlineState.append("</op:willingness><contact priority=\"0.8\">sip:blum@open-ims.org</contact>");
			onlineState.append("<note xml:lang=\"en\"></note>");
			onlineState.append("<timestamp>2008-07-18T16:39:09.437Z</timestamp>");
			onlineState.append("</tuple><dm:person id=\"p1\"><op:overriding-willingness><op:basic>open</op:basic>");
			onlineState
					.append("</op:overriding-willingness><dm:note xml:lang=\"en\"></dm:note><dm:timestamp>2008-07-18T16:39:09.437Z</dm:timestamp></dm:person></presence>");

			try
			{
				Publication pub = coreservice.createPublication(null, params[0], "presence");
				pub.setListener(ConsoleUE.this);
				pub.publish(onlineState.toString().getBytes(), "application/pidf+xml");
			} catch (IllegalArgumentException e1)
			{
				e1.printStackTrace();
			} catch (ServiceClosedException e1)
			{
				e1.printStackTrace();
			} catch (ImsException e1)
			{
				e1.printStackTrace();
			}
		}
	}

	private class UnpublishCommand extends Command
	{
		public UnpublishCommand()
		{
			super(new String[]
			{ "up", "upub", "unpub", "unpublish" }, "               - UnPublish own presence information");
		}

		public void execute(String[] params)
		{
		}

	}

	private class MessageCommand extends Command
	{
		public MessageCommand()
		{
			super(new String[]
			{ "m", "msg", "message" }, "<uri> <msg>    - Send to <uri> the message <msg>");
		}

		public void execute(String[] params)
		{
			if (params.length < 2)
			{
				System.out.println("Parameter count wrong");
				return;
			}
			StringBuffer msg = new StringBuffer(params[1]);
			for (int i = 2; i < params.length; i++)
			{
				msg.append(" ");
				msg.append(params[i]);
			}
			PageMessage pageMessage;
			try
			{
				pageMessage = coreservice.createPageMessage(null, toUserURI(params[0]));
				pageMessage.setListener(ConsoleUE.this);
				pageMessage.send(msg.toString().getBytes(), "text/plain");
			} catch (ServiceClosedException e)
			{
				e.printStackTrace();
			} catch (IllegalStateException e)
			{
				e.printStackTrace();
			} catch (IllegalArgumentException e)
			{
				e.printStackTrace();
			}
		}
	}

	private class MakeCallCommand extends Command
	{
		public MakeCallCommand()
		{
			super(new String[]
			{ "c", "call" }, "<uri>          - Call <uri>");
		}

		public void execute(String[] params)
		{
			if (params.length < 1)
			{
				System.out.println("Parameter count wrong");
				return;
			}
			try
			{
				Session session = coreservice.createSession(params[0], toUserURI(params[1]));
				session.setListener(ConsoleUE.this);
				StreamMedia myMedia = (StreamMedia) session.createMedia("StreamMedia", Media.DIRECTION_SEND_RECEIVE);
				myMedia.setStreamType(StreamMedia.STREAM_TYPE_VIDEO);
				myMedia.setSource("capture://video");
				sessions.add(session);
				session.start();
			} catch (ImsException ie)
			{
				ie.printStackTrace();
			} catch (ServiceClosedException e)
			{
				e.printStackTrace();
			}
		}
	}

	private abstract class CallCommand extends Command
	{
		protected String[] params;

		public CallCommand(String[] name, String help)
		{
			super(name, help);
		}

		public void execute(String[] params)
		{
			if (params.length < 1)
			{
				System.out.println("Parameter count wrong");
				return;
			}
			Integer k = toIntParam(params[0]);
			if (k == null)
			{
				System.out.println("Parameter has to be of type Integer");
				return;
			}
			this.params = params;
			Session s;
			if (k.intValue() >= sessions.size() || k.intValue() < 0 || (s = (Session) sessions.get(k.intValue())) == null)
			{
				System.out.println("Session with id: " + params[0] + " does not exist. Print the call sessions table to get more info.");
			} else
			{
				execute(s);
			}
		}

		public abstract void execute(Session session);
	}

	private class AbortCommand extends CallCommand
	{
		public AbortCommand()
		{
			super(new String[]
			{ "a", "abort" }, "<id>           - Abort call with <id> (while ringing)");
		}

		public void execute(Session session)
		{
			session.terminate();
		}
	}

	private class AnswerCommand extends CallCommand
	{
		public AnswerCommand()
		{
			super(new String[]
			{ "y", "yes", "answer" }, "<id>           - Answer call with <id>");
		}

		public void execute(Session session)
		{
			Media[] temp = session.getMedia();
			if (temp == null || temp.length == 0)
			{
				StreamMedia media;
				try
				{
					media = (StreamMedia) session.createMedia("StreamMedia", Media.DIRECTION_SEND_RECEIVE);
					media.setStreamType(StreamMedia.STREAM_TYPE_AUDIO);
				} catch (ImsException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			session.accept();

			Media[] media = session.getMedia();
			for (int i = 0; i < media.length; i++)
			{
				if (media[i] instanceof FramedMedia)
					((FramedMedia) media[i]).setListener(ConsoleUE.this);
			}
		}
	}

	private class RejectCommand extends CallCommand
	{
		public RejectCommand()
		{
			super(new String[]
			{ "n", "no", "reject" }, "<id>           - Reject call with <id>");
		}

		public void execute(Session session)
		{
			session.reject();
		}
	}

	private class HangupCommand extends CallCommand
	{
		public HangupCommand()
		{
			super(new String[]
			{ "h", "hang", "hangup" }, "<id>           - Hang-up call with <id>");
		}

		public void execute(Session session)
		{
			session.terminate();
		}

	}

	private class HoldCommand extends CallCommand
	{
		public HoldCommand()
		{
			super(new String[]
			{ "hold" }, "<id>           - Holds the call");
		}

		public void execute(Session session)
		{
			Media[] media = session.getMedia();

			for (int i = 0; i < media.length; i++)
			{
				media[i].setDirection(Media.DIRECTION_SEND);
			}

			try
			{
				session.update();
			} catch (ImsException e)
			{
				e.printStackTrace();
			}
		}
	}

	private class ResumeCommand extends CallCommand
	{
		public ResumeCommand()
		{
			super(new String[]
			{ "resume" }, "Resume the call");
		}

		public void execute(Session session)
		{
			Media[] media = session.getMedia();

			for (int i = 0; i < media.length; i++)
			{
				media[i].setDirection(Media.DIRECTION_SEND_RECEIVE);
			}

			try
			{
				session.update();
			} catch (ImsException e)
			{
				e.printStackTrace();
			}
		}
	}

	private class StartChatCommand extends Command
	{
		public StartChatCommand()
		{
			super(new String[]
			{ "chat" }, "Starts a MSRP chat");
		}

		public void execute(String[] params)
		{
			try
			{
				Session session = coreservice.createSession(null, toUserURI(params[0]));
				session.createMedia("FramedMedia", Media.DIRECTION_INACTIVE);
				session.setListener(ConsoleUE.this);
				session.start();
				sessions.add(session);

			} catch (ServiceClosedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ImsException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private class UpdateChatCommand extends CallCommand
	{
		public UpdateChatCommand()
		{
			super(new String[]
			{ "uchat" }, "Starts a MSRP chat");
		}

		public void execute(Session session)
		{
			try
			{
				((FramedMedia)session.getMedia()[0]).setDirection(Media.DIRECTION_SEND_RECEIVE);
				session.update();
			} catch (ImsException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private class AddChatCommand extends CallCommand
	{
		public AddChatCommand()
		{
			super(new String[]
			{ "achat" }, "Adds chat to the given session");
		}

		public void execute(Session session)
		{
			try
			{
				session.createMedia("FramedMedia", Media.DIRECTION_SEND_RECEIVE);
				session.update();
			} catch (ImsException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private class RemoveChatCommand extends CallCommand
	{
		public RemoveChatCommand()
		{
			super(new String[]
			{ "rchat" }, "Removes the chat from the given session");
		}

		public void execute(Session session)
		{
			try
			{
				Media[] media = session.getMedia();
				for (int i = 0; i < media.length; i++)
				{
					if (media[i] instanceof FramedMedia)
					{
						session.removeMedia(media[i]);
					}
				}

				if (session.hasPendingUpdates())
					session.update();
			} catch (ImsException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private class SayCommand extends CallCommand
	{
		public SayCommand()
		{
			super(new String[]
			{ "say" }, "Says somethings in the chat");
		}

		public void execute(Session session)
		{
			if (params.length < 2)
			{
				System.out.println("Parameter count wrong");
				return;
			}
			StringBuffer msg = new StringBuffer(params[1]);
			for (int i = 2; i < params.length; i++)
			{
				msg.append(" ");
				msg.append(params[i]);
			}

			try
			{
				Media[] media = session.getMedia();
				for (int i = 0; i < media.length; i++)
				{
					if (media[i] instanceof FramedMedia)
					{
						((FramedMedia) media[i]).sendBytes(msg.toString().getBytes("utf-8"), "text/plain", null);
					}
				}
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private class ListCallsCommand extends Command
	{
	    private String[] mediaStates = new String[]{"", "INACTIVE", "PENDING", "ACTIVE", "DELETED", "PROPOSAL"};
		private String[] updateStates = new String[]{"", "UNCHANGED","MODIFIED", "REMOVED"};
		
		public ListCallsCommand()
		{
			super(new String[]
			{ "l", "list" }, "               - List Call sessions table");
		}

		public void execute(String[] params)
		{
			System.out.println("--- List of current sessions ---");
			StringBuffer stb = new StringBuffer();

			int i2 = 0;
			Iterator iter2 = sessions.iterator();
			while (iter2.hasNext())
			{
				Session session = (Session) iter2.next();
				stb.append(i2++);
				stb.append(": ");
				stb.append(session.getRemoteUserId());
				
				stb.append(" Streams: \r\n");
				
				Media[] media = session.getMedia();
				for (int i = 0; i < media.length; i++)
				{
					stb.append(media[i].getClass());
					stb.append(" State: ");
					stb.append(mediaStates[media[i].getState()]);
					stb.append(" Update State: ");
					stb.append(updateStates[media[i].getUpdateState()]);
					stb.append("\r\n");
				}	
			}
			System.out.print(stb.toString());
			System.out.println("---------------------------------");

		}
	}

	private class ListSubscriptions extends Command
	{
		public ListSubscriptions()
		{
			super(new String[]
			{ "ls", "listsubs" }, "               - List Subscriptions with status");
		}

		public void execute(String[] params)
		{
			System.out.println("--- List of current subscriptions ---");
			Collection slist = coreservice.getSubscriptions().getSubscriptions();

			Iterator iter = slist.iterator();
			int i = 0;
			while (iter.hasNext())
			{
				Subscription sub = (Subscription) iter.next();
				try
				{
					System.out.println(i++ + ". " + sub.getRemoteUserId());
				} catch (IllegalArgumentException e)
				{
					e.printStackTrace();
				}

			}
			System.out.println("--------------------------------------");
		}
	}

	private class RefersToCommand extends Command
	{

		public RefersToCommand()
		{
			super(new String[]
			{ "ref" }, " sends a refer request");
		}

		public void execute(String[] params)
		{
			String to = toUserURI(params[0]);
			String refto = toUserURI(params[1]);
			String method = params[2];

			try
			{
				Reference reference = coreservice.createReference(null, to, refto, method);
				reference.setListener(new ReferenceListener()
				{
					public void referenceDelivered(Reference reference)
					{
						System.out.println("Reference delivered");
					}

					public void referenceDeliveryFailed(Reference reference)
					{
						System.out.println("Reference delivery failed");
					}

					public void referenceNotify(Reference reference, Message notify)
					{
						System.out.println("Reference Notify");
					}

					public void referenceTerminated(Reference reference)
					{
						System.out.println("Reference terminated");
					}
				});
				reference.refer();
			} catch (ServiceClosedException e)
			{
				e.printStackTrace();
			}
		}
	}

	private class AddStreamMedia extends CallCommand
	{
		public AddStreamMedia()
		{
			super(new String[]
			{ "asm", "addstreammedia" }, "<id> <audio | video>");
		}

		public void execute(Session session)
		{
			try
			{
				String mediaType = params[1];

				StreamMedia media = (StreamMedia) session.createMedia("StreamMedia", Media.DIRECTION_SEND_RECEIVE);
				if (mediaType.equals("audio"))
				{
					media.setStreamType(StreamMedia.STREAM_TYPE_AUDIO);
				} else if (mediaType.equals("video"))
				{
					media.setStreamType(StreamMedia.STREAM_TYPE_VIDEO);
				}
				
				session.update();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private class RemoveStreamMedia extends CallCommand
	{
		public RemoveStreamMedia()
		{
			super(new String[]
			{ "rsm", "removestreammedia" }, "<call> <media index>");
		}

		public void execute(Session session)
		{
			try
			{
				int mediaIndex = Integer.parseInt(params[1]);
				
				Media[] media = session.getMedia();
				session.removeMedia(media[mediaIndex]);
				session.update();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void notifyDelivered(Subscription subscription)
	{
		System.err.println("Delivered");
	}

	public void notifyDeliveredFailed(Subscription subscription)
	{
		// TODO Auto-generated method stub
		
	}

	public void capabilityReceived(CoreService coreservice,
			Capabilities capability) {
		// TODO Auto-generated method stub
		
	}

	


}

class StatusTypes
{

	public static final int ONLINE = 0;
	public static final int OFFLINE = 1;
	public static final int AWAY = 2;
	public static final int BUSY = 3;
	public static final int WORKING = 4;
	public static final int ON_PHONE = 5;
	public static final int IN_MEETING = 6;
	public static final int NOT_KNOWN = 7;

	private static final String[][] status =
	{
	{ "online", "on" },
	{ "offline", "off" },
	{ "away", "aw" },
	{ "busy", "bs" },
	// {"working", "work", "wk"},
	// {"on-phone", "phone", "ph"},
	// {"meeting", "mt"}
	};

	public static int getStatus(String param)
	{
		int statusInt = NOT_KNOWN;
		int i, j;

		String statusLower = param.toLowerCase();
		for (i = 0; i < status.length; i++)
			for (j = 0; j < status[i].length; j++)
				if (statusLower.equals(status[i][j]))
				{
					statusInt = i;
					break;
				}

		return statusInt;
	}

	public static String getStatusHelp()
	{
		StringBuffer sBuffer = new StringBuffer("Possible status parameters:\n");
		sBuffer.append("\t");

		for (int i = 0; i < status.length; i++)
		{
			sBuffer.append(status[i][0]);
			if (status[i].length > 1)
				sBuffer.append(" (alias: ");
			for (int j = 1; j < status[i].length; j++)
			{
				if (j > 1)
					sBuffer.append(",");
				sBuffer.append(status[i][j]);
			}
			if (status[i].length > 1)
				sBuffer.append(")\n");
		}

		return sBuffer.toString();
	}
}
