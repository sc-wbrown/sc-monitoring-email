package com.stonecobra.monitoring;


import java.io.*;
import java.util.*;
import javax.mail.*;
import javax.mail.search.FlagTerm;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * This class allows users to obtain mailbox sessions either through IMAP or POP3 protocols, read mail and/or make a copy 
 * @author Weslee Brown - wbrown@stonecobra.com
 *
 */
public class Email
{	
	private Properties emailConfiguration = new Properties();
	private static final String propFile = "config.properties";
	private static final String IMAP = "mail.imap.host";
	private static final String POP = "mail.pop3.host";
	private static final String IMAP_USER = "mail.imap.user";
	private static final String POP_USER = "mail.pop3.user";
	private static final String STORE = "mail.store.protocol";
	private static final String IMAP_FOLDER = "mail.imap.folder";
	static Logger log = Logger.getLogger(Email.class.getName());

	public Email(){
	}
	
	/**
	 * This method will clear any configurations set by the user and fetch any properties saved in the config.properties file 
	 */
	public void refreshConfiguration()
	{
		emailConfiguration.clear();
		fetchConfiguration();	
	}
	
	/**
	 * This method will load the config.properties file into the emailConfiguration Property
	 */
	private void fetchConfiguration()
	{
		try
		{
			InputStream inProps =  new FileInputStream(propFile);
			emailConfiguration.load(inProps);
		}
		catch(IOException e)
		{
			log.error(e);
		}
	}
	
	/**
	 * This method gets the Properties object built from the config.properties file
	 * @return emailConfiguration, the Properties object 
	 */
	public Properties getProps()
	{
		return emailConfiguration;
	}
	
	/**
	 * This method gets the host set in the config.properties file
	 * @return the host information as a String
	 */
	public String getIMAPHost()
	  { return emailConfiguration.getProperty(IMAP); }
	
	public String getPOP3Host() {
		return emailConfiguration.getProperty(POP);
	}
	/**
	 * This method gets the user set in the config.properties file
	 * @return the user's email address as a String 
	 */
	public String getIMAPUser()
	  { return emailConfiguration.getProperty(IMAP_USER); }
	
	public String getPOP3User() {
		return emailConfiguration.getProperty(POP_USER);
	}
	/**
	 * This method gets the protocol set in the config.properties file
	 * @return the protocol used as a String
	 */
	public String getProtocol()
	  { return emailConfiguration.getProperty(STORE); }
	
	/**
	 * This method is used to set the host name in the config.properties file
	 * @param host, the desired email host to set in the config.properties file, as a String
	 */
	public void setIMAPHost(String host)
	  { emailConfiguration.setProperty(IMAP, host); }
	
	public void setPOP3Host(String host) {
		emailConfiguration.setProperty(POP, host);
	}
	/**
	 * This method is used to set the user's email address in the config.properties file
	 * @param user, the desired email address to use, as a String
	 */
	public void setIMAPUser(String user)
	  { emailConfiguration.setProperty(IMAP_USER, user); }
	
	public void setPOP3User(String user) {
		emailConfiguration.setProperty(POP_USER, user);
	}
	/**
	 * This method is used to set the email protocol to communicate with the server in the config.properties file
	 * @param protocol, the desired protocol to use, as a String
	 */
	public void setProtocol(String protocol)
	  { emailConfiguration.setProperty(STORE, protocol); }
	
	public int getMessageCount(Folder folder) throws Exception {
        return folder.getMessageCount();
    }
	
    public int getNewMessageCount(Folder folder) throws Exception {
        return folder.getNewMessageCount();
    }
	
	public void openFolder(String folderName, Store store) throws Exception {
	         Folder folder;
	        // Open the Folder
	        folder = store.getDefaultFolder();
	         
	        folder = folder.getFolder(folderName);
	         
	        if (folder == null) {
	            log.error("Invalid Folder");
	        }
	         
	        // try to open read/write and if that fails try read-only
	        try {
	             
	            folder.open(Folder.READ_WRITE);
	             
	        } catch (MessagingException ex) {
	             
	            folder.open(Folder.READ_ONLY);
	             
	        }
	    }
	     
	/**
	 * This method will use the host, email, and password to connect to an email inbox  
	 * @param host, the host to connect to
	 * @param email, the users email address 
	 * @param password, the users email address password
	 * @return Store object, for accessing and retrieving messages
	 */
	public Store signIn(String host, String email, String password)
	{
		Session session = Session.getDefaultInstance(emailConfiguration, null);
		Store store = null;
		try 
		{
			store = session.getStore();
			try 
			{
				store.connect(host, email, password);
				return store;
				
			} catch (MessagingException e) 
			{
				log.error(e);
			}
		} catch (NoSuchProviderException e) 
		{
			log.error(e);
		}
		return store;
	}
	
	/**
	 * This method will list the available folders in the users email account, along with their corresponding index
	 * @param store, the Store object containing the users email account folders
	 * @return allFolders, the array of Folders for the current user
	 */
	public Folder selectIMAPFolder(Store store)
	{
		int selection = 0;
		Scanner in = new Scanner(System.in);
		Folder[] allFolders = null;
		try 
		{
			allFolders = store.getDefaultFolder().list("*");
		} catch (MessagingException e) 
		{
			log.error(e);
		}
		if(allFolders != null)
		{
			System.out.println("Available Folders ");
			while(selection > allFolders.length - 1 || selection < 1)
			{
				for(int i = 1; i < allFolders.length; i++)
				{
					System.out.println(i + " " + allFolders[i]);
				}
				System.out.print("Select a folder: ");
				if(in.hasNextInt())
				{
					selection = in.nextInt();
				}
				else
				{
					in.next();
				}
			}
			emailConfiguration.put(IMAP_FOLDER, allFolders[selection]);
		}
		return allFolders[selection];
	}
	
	/**
	 * This method will allow the user to select the folder they wish to look through 
	 * @param folders, the array of Folders to select from
	 * @param index, the integer value of the index of the desired folder
	 * @return the Folder the user would like to look through
	 */
	public Folder setIMAPFolder(Folder[] folders, int index)
	{
		Folder folder = null;
		folder = folders[index];
		emailConfiguration.put(IMAP_FOLDER, folders[index]);
		return folder;
	}
	
	public Folder setIMAPFolder(Folder folder)
	{
		emailConfiguration.put(IMAP_FOLDER, folder);
		return folder;
	}
	
	public Message[] getUnreadMail(Folder inbox)
	{
		Message[] messages = null;
		if(inbox!=null)
		{ 	
			try 
			{
				inbox.open(Folder.READ_ONLY);
				FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false); 
				messages = inbox.search(ft);
			} catch (MessagingException e) 
			{
				log.error(e);
			}
		}
		return messages;
	}
	
	/**
	 * This method will extract the ID(Email Folder Name), SUBJECT(Email heading), and BODY(Email body) of each unread 
	 * message from the inbox Folder parameter, place each into an individual JSONObject, and all of the JSONObjects 
	 * into a JSONArray object  
	 * @param inbox, the desired Folder to extract the email messages from 
	 * @return the JSONArray object of all of the emails extracted from the Folder parameter
	 */
	public JSONArray getUnreadMailJSON(Folder inbox)
	{
		JSONArray array = new JSONArray();
		if(inbox!=null)
		{
			try 
			{
				inbox.open(Folder.READ_WRITE);
				FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false); 
				Message messages[] = inbox.search(ft);
				for(Message msg:messages)
				{
					JSONObject obj = new JSONObject();
					LinkedList list = new LinkedList();
					inbox.setFlags(new Message[] {msg}, new Flags(Flags.Flag.SEEN), true);
					obj.put("type", "Email");
					obj.put("fields", list);
					Enumeration headers = msg.getAllHeaders();
							while(headers.hasMoreElements())
							{
								JSONObject headerObj = new JSONObject();
								Header h = (Header)headers.nextElement();
								headerObj.put( h.getName(), h.getValue() );
								list.add(headerObj);
							}
					try 
		            {
						obj.put("data", msg.getContent());
					} catch (IOException e) 
					{
						log.error(e);
					}		
					array.add(obj);
				}
			} 
			catch (MessagingException e) 
			{
				log.error(e);
			}
		}
		return array;
	}
	
	public Message[] getAllMail(Folder inbox)
	{
		Message[] messages = null;
		if(inbox!=null)
		{ 	
			try 
			{
				inbox.open(Folder.READ_ONLY); 
				messages = inbox.getMessages();
			} catch (MessagingException e) 
			{
				log.error(e);
			}
		}
		return messages;
	}
	
	/**
	 * This method will take an mailbox instance
	 * @param store
	 * @return
	 */
	public JSONArray copyMessage(Store store) {

		Folder folder;
		Message message = null;
		String subject = null;
		Object content = null;
		JSONArray array = new JSONArray();

		try {
			folder = store.getFolder("INBOX");
			folder.open(Folder.READ_WRITE);
			for(int i = 1; i <= folder.getMessageCount(); i++) {
				JSONObject obj = new JSONObject();

				message = folder.getMessage(i);
				subject = message.getSubject();
				obj.put("subject", subject);
				try {
					content = message.getContent();
					obj.put("body", content);
				} catch (IOException e) {
					log.error(e);
				}
				array.add(obj);
			}
			return array;
		} catch (MessagingException e) {
			log.error(e);
		}
		return array;
	}
	
}