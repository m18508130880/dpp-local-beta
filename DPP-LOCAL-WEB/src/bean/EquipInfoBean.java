package bean;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import rmi.Rmi;
import rmi.RmiBean;
import util.*;

public class EquipInfoBean extends RmiBean 
{
	public final static long serialVersionUID = RmiBean.RMI_EQUIP_INFO;
	public long getClassId()
	{
		return serialVersionUID;
	}
	
	public EquipInfoBean()
	{
		super.className = "EquipInfoBean";
	}
	
	public void ExecCmd(HttpServletRequest request, HttpServletResponse response, Rmi pRmi, boolean pFromZone) throws ServletException, IOException
	{
		getHtmlData(request);
		currStatus = (CurrStatus)request.getSession().getAttribute("CurrStatus_" + Sid);
		currStatus.getHtmlData(request, pFromZone);
		
		msgBean = pRmi.RmiExec(currStatus.getCmd(), this, 0, 25);
		switch(currStatus.getCmd())
		{
			case 40://添加/编辑
				currStatus.setResult(MsgBean.GetResult(msgBean.getStatus()));
				msgBean = pRmi.RmiExec(0, this, 0, 25);
			case 0://查询
			case 1:
		    	request.getSession().setAttribute("Equip_Info_" + Sid, ((Object)msgBean.getMsg()));
		    	
		    	DevGJBean devGJBean = new DevGJBean();
		    	msgBean = pRmi.RmiExec(1, devGJBean, 0, 25);
		    	request.getSession().setAttribute("DevGJ_All_" + Sid, ((Object)msgBean.getMsg()));		    	
		    	currStatus.setJsp("Equip_Info.jsp?Sid=" + Sid);		    
		    	break;
		}
		request.getSession().setAttribute("CurrStatus_" + Sid, currStatus);
	   	response.sendRedirect(currStatus.getJsp());
	}
	
	public void IdCheck(HttpServletRequest request, HttpServletResponse response, Rmi pRmi, boolean pFromZone)
	{
		try 
		{
			getHtmlData(request);
			currStatus = (CurrStatus)request.getSession().getAttribute("CurrStatus_" + Sid);
			currStatus.getHtmlData(request, pFromZone);
			
			PrintWriter outprint = response.getWriter();
			String Resp = "3006";
			
			msgBean = pRmi.RmiExec(2, this, 0, 25);//查找是否有该设备存在
			System.out.println("msgBean.getStatus():" + msgBean.getStatus());
			switch(msgBean.getStatus())
			{
				case 0://已存在
					Resp = "3006";
					break;
				default://可用
					Resp = "0000";
					break;
			}
			
			request.getSession().setAttribute("CurrStatus_" + Sid, currStatus);
			outprint.write(Resp);
		}
		catch (Exception Ex)
		{
			Ex.printStackTrace();
		}
	}
		
	public String getSql(int pCmd)
	{
		String Sql = "";
		switch (pCmd)
		{
			case 0://查询                
				Sql = " select  t.id, t.cname, t.project_Id, t.project_name, t.g_id " +
					  " from view_equip_info t order by t.id";
				break;
			case 3://User设备查询                
				Sql = " select  t.id, t.cname, t.project_Id, t.project_name, t.g_id " +
					  " from view_equip_info t where t.project_Id='" + currStatus.getFunc_Project_Id() + "'  order by t.id";
				break;	
			case 2://设备ID检测
				Sql = " select  t.id, t.cname, t.project_Id, t.project_name, t.g_id " +
					  " from view_equip_info t " +
					  " where upper(Id) = upper('"+ Id +"') ";
				break;
			case 40://编辑设备EquipInfo
				Sql = "{call pro_update_equip('" + Id + "', '" + CName + "', '" + Pre_Id + "', '" + Pre_Project_Id + "', '" + After_Id + "', '" + After_Project_Id + "')}";
				break;
		}
		return Sql;
	}
	
	public boolean getData(ResultSet pRs)
	{
		boolean IsOK = true;
		try
		{
			setId(pRs.getString(1));
			setCName(pRs.getString(2));
			setProject_Id(pRs.getString(3));
			setProject_Name(pRs.getString(4));
			setG_Id(pRs.getString(5));
		}
		catch (SQLException sqlExp)
		{
			sqlExp.printStackTrace();
		}
		return IsOK;
	}
	
	public boolean getHtmlData(HttpServletRequest request)
	{
		boolean IsOK = true;
		try
		{
			setId(CommUtil.StrToGB2312(request.getParameter("Id")));
			setCName(CommUtil.StrToGB2312(request.getParameter("CName")));
			setProject_Id(CommUtil.StrToGB2312(request.getParameter("Project_Id")));
			setProject_Name(CommUtil.StrToGB2312(request.getParameter("Project_Name")));
			setG_Id(CommUtil.StrToGB2312(request.getParameter("G_Id")));
			setSid(CommUtil.StrToGB2312(request.getParameter("Sid")));
			setPre_Id(CommUtil.StrToGB2312(request.getParameter("Pre_Id")));
			setPre_Project_Id(CommUtil.StrToGB2312(request.getParameter("Pre_Project_Id")));
			setAfter_Id(CommUtil.StrToGB2312(request.getParameter("After_Id")));
			setAfter_Project_Id(CommUtil.StrToGB2312(request.getParameter("After_Project_Id")));
		}
		catch (Exception Exp)
		{
			Exp.printStackTrace();
		}
		return IsOK;
	}
	
	private String Id;
	private String CName;
	private String Project_Id;
	private String Project_Name;
	private String G_Id;
	
	private String Sid;
	private String Pre_Id;
	private String After_Id;
	private String Pre_Project_Id;
	private String After_Project_Id;
    
	
	public String getPre_Id() {
		return Pre_Id;
	}

	public void setPre_Id(String pre_Id) {
		Pre_Id = pre_Id;
	}

	public String getAfter_Id() {
		return After_Id;
	}

	public void setAfter_Id(String after_Id) {
		After_Id = after_Id;
	}

	public String getPre_Project_Id() {
		return Pre_Project_Id;
	}

	public void setPre_Project_Id(String pre_Project_Id) {
		Pre_Project_Id = pre_Project_Id;
	}

	public String getAfter_Project_Id() {
		return After_Project_Id;
	}

	public void setAfter_Project_Id(String after_Project_Id) {
		After_Project_Id = after_Project_Id;
	}

	public String getG_Id() {
		return G_Id;
	}

	public void setG_Id(String g_Id) {
		G_Id = g_Id;
	}

	public String getProject_Name() {
		return Project_Name;
	}

	public void setProject_Name(String project_Name) {
		Project_Name = project_Name;
	}

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public String getCName() {
		return CName;
	}

	public void setCName(String cName) {
		CName = cName;
	}

	public String getProject_Id() {
		return Project_Id;
	}

	public void setProject_Id(String project_Id) {
		Project_Id = project_Id;
	}

	
	public String getSid() {
		return Sid;
	}

	public void setSid(String sid) {
		Sid = sid;
	}
	
	
}