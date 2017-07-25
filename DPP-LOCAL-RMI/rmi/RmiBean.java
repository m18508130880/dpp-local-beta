package rmi;

import java.io.Serializable;
import java.sql.ResultSet;
import util.*;


/**RmiBean ʵ���� serializable �����л��ӿ�
 * @author Cui
 * bean������඼�̳� RmiBean
 * ΪʲôҪ�����л�?
 *     ���Ҫͨ��Զ�̵ķ������ã�RMI��ȥ����һ��Զ�̶���ķ��������ڼ����A�е���
 *     ��һ̨�����B�Ķ���ķ�������ô����Ҫͨ��JNDI�����ȡ�����BĿ���������ã�
 *     �������B���͵�A������Ҫʵ�����л��ӿ�
 */
public abstract class RmiBean implements Serializable
{
	public final static String UPLOAD_PATH = "/www/DPP-LOCAL/DPP-LOCAL-WEB/files/excel/";
	
	/**************************system**********************/

	
	/**************************admin***********************/
	public static final int	RMI_DEVGJ					= 11;
	public static final int	RMI_DEVGX					= 12;
	public static final int RMI_USER_INFO			 	= 13;
	public static final int RMI_USER_ROLE		   	    = 14;	
	public static final int RMI_PROJECT_INFO			= 15;
	public static final int RMI_EQUIP_INFO			    = 16;
	
	public static final int RMI_USER_POSITION			= 18;
	public static final int RMI_CORP_INFO			    = 19;
	
	/**************************user-data*******************/
	public static final int RMI_DATA			        = 30;
	public static final int	RMI_DATAGJ					= 31;
	public static final int	RMI_DATAGX					= 32;
	
	public MsgBean    msgBean = null;
	public String     className;
	public CurrStatus currStatus = null;
	
	public RmiBean()
	{
		msgBean = new MsgBean(); 		
	}
	
	public String getClassName()
	{
		return className;
	}
	
	public abstract long getClassId();
	public abstract String getSql(int pCmd);
	public abstract boolean getData(ResultSet pRs);
}
