<%@ page contentType="text/html; charset=gb2312" %>
<%@ page import="java.util.*" %>
<%@ page import="bean.*" %>
<%@ page import="util.*" %>
<%@ page import="java.lang.*" %>
<%@ page import="java.math.*" %>
<%@ page import="java.text.*" %>
<%@ taglib uri="/WEB-INF/limitvalidatetag.tld" prefix="Limit"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>����ܾ���Ϣ��ʾ</title>
<meta http-equiv="x-ua-compatible" content="ie=7"/>
<link type="text/css" href="../skin/css/style.css" rel="stylesheet"/>
<script type='text/javascript' src='../skin/js/zDrag.js'   charset='gb2312'></script>
<script type='text/javascript' src='../skin/js/zDialog.js' charset='gb2312'></script>
</head>
<%
	
	String Sid = CommUtil.StrToGB2312(request.getParameter("Sid"));		
	String Type = "";
	String pId   ="";
  CurrStatus currStatus    = (CurrStatus)session.getAttribute("CurrStatus_" + Sid);
  ArrayList  Dev_GJ        = (ArrayList)session.getAttribute("One_GJ_" + Sid); 
%>
<body style=" background:#CADFFF">
<form name="Dev_GJ"  action="Select_GJ.do" method="post" target="mFrame">
<div>
	<table width="100%" border=1 cellPadding=0 cellSpacing=0 bordercolor="#3491D6" borderColorDark="#ffffff">					
		<%
								if(null != Dev_GJ)
															{
																Iterator deviter = Dev_GJ.iterator();
																while(deviter.hasNext())
																{
																	DevGJBean dBean = (DevGJBean)deviter.next();
																	pId = dBean.getId();
							%>   		    	
					 <tr height='20'>
							<td width="35px" align='center' >����</td>
							<td ><%= dBean.getId()%>&nbsp; </td>
					  </tr>
					
						<tr height='20'>
							<td width="35px" align='center' >����</td>
							<td ><%= dBean.getTop_Height()%>&nbsp;</td>
						</tr>
						<tr height='20'>
							<td width="35px"  align='center' >�׸�</td>
							<td ><%= dBean.getBase_Height()%>&nbsp;</td>
						</tr>
					  <tr height='20'>
							<td width="35px" align='center' >����</td>
							<td ><%= dBean.getMaterial()%> &nbsp;</td>
					  </tr>
					  <tr height='20'>
							<td width="35px" align='center' >ˮλ</td>
							<td ><%= dBean.getWaterLev()%> &nbsp; </td>
					  </tr>																
					<%
						}
					}
		%>					
			<tr>
					<td colspan='21' align='center'>
						<a href='#' onClick="doDel('<%=pId%>','<%=Type%>')"><font color='red'>ȡ����ע</font></a>
					</td>	
				</tr>							
		</table>	
</div>
</form>
</body>
<SCRIPT LANGUAGE=javascript>
var reqDel = null;
function doDel(pId, pType)
{
	if(confirm('ȷ��ɾ����ǰվ���ע?'))
	{
		if(window.XMLHttpRequest)
	  {
			reqDel = new XMLHttpRequest();
		}
		else if(window.ActiveXObject)
		{
			reqDel = new ActiveXObject("Microsoft.XMLHTTP");
		}
		reqDel.onreadystatechange = function()
		{
		  var state = reqDel.readyState;
		  if(state == 4)
		  {
		    if(reqDel.status == 200)
		    {
		      var Resp = reqDel.responseText;
		      if(null != Resp && Resp.substring(0,4) == '0000')  //��⵽������,ִ�д��ڹر�
		      {		      	
		      	alert('ɾ����ע�ɹ�!��ˢ�µ�ͼ!');		      	
		    		return;
		      }  
		      else
		      {
		      	alert('ɾ����עʧ��!');
		    		return;
		      }   
		    }
		    else
		    {
		    	alert('ɾ����עʧ��!');
		    	return;
		    }
		  }
		};
		var url = "Admin_Drag_GJ.do?Cmd=16&Sid=<%=Sid%>&Id="+pId+"&currtime="+new Date();
		reqDel.open("POST",url,true);
		reqDel.send(null);
		return true;
	}
}

</SCRIPT>
</html>