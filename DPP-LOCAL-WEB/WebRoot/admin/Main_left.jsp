<%@ page contentType="text/html; charset=gb2312"%>
<%@ page import="java.util.*"%>
<%@ page import="bean.*"%>
<%@ page import="util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>��߹��ܲ˵�</title>
<meta http-equiv="x-ua-compatible" content="ie=7"/>
<link type="text/css" href="../skin/css/style.css" rel="stylesheet"/>
<script type="text/javascript" src="../skin/js/util.js"></script>
<script type="text/javascript" src="../skin/js/day.js"></script>

<script language=javascript>document.oncontextmenu=function(){window.event.returnValue=false;};</script>
<%
	
	String Sid = CommUtil.StrToGB2312(request.getParameter("Sid"));
	
%>
</head>
<body style="background:#0B80CC;">
<div id="PARENT">
	<ul id="nav">
		<li>
			<a href="#" onClick="doCorp_Info()"                                       >��˾��Ϣ</a>
		</li>			
		<li>
			<a href="#" onClick="doUser_Info()"                                       >�û�����</a>
		</li>		
		<li>
			<a href="#" onClick="doProject_Info()"                                    >��Ŀ����</a>
		</li>	
		<li>
			<a href="#" onClick="doGJ_Load()"                                         >����¼��</a>
		</li>	
		<li id="li04" class="panel-body"  ><a href="#" onClick="DoMenu('UserMenu2')">���߹���</a></li>	 
		<ul id="UserMenu2" class="collapsed">	
			<li id="Display0201"><a href="#" onClick="doGIS_GJ()" 	   >���ߵ�ͼ</a></li>
			<li id="Display0202"><a href="#" onClick="doGJ_Info()"	   >�ܾ��б�</a></li>
			<li id="Display0203"><a href="#" onClick="doGX_Info()"  	 >�����б�</a></li>
			<li id="Display0204"><a href="#" onClick="doEquip_Info()"  >�豸����</a></li>
 	 	</ul>
	  </li>
	  <!--
	 <li><a href="#" onClick="doDiaphic()"    >ͼ�����</a></li>	-->
	</ul>
</div>
</body>
<SCRIPT LANGUAGE=javascript>
//--------------�˵�����----------------------------
var LastLeftID = "";
function DoMenu(emid)
{
	 var obj = document.getElementById(emid); 
	 obj.className = (obj.className.toLowerCase() == "expanded"?"collapsed":"expanded");
	 if((LastLeftID!="")&&(emid!=LastLeftID)) //�ر���һ��Menu
	 {
	  	document.getElementById(LastLeftID).className = "collapsed";
	 }
	 LastLeftID = emid;
}

function DoHide(emid)
{ 
	 document.getElementById(emid).className = "collapsed";	 
}

function doPage_Build()
{
   window.parent.frames.mFrame.location = 'Page_Build.jsp';
}
//--------------------��˾��Ϣ----------------------
function doCorp_Info()
{
	window.parent.frames.mFrame.location = 'Admin_Corp_Info.do?Cmd=0&Sid=<%=Sid%>';
}
//-------------------�û�����-----------------------
function doUser_Info()
{	
	window.parent.frames.mFrame.location = 'Admin_User_Info.do?Cmd=1&Func_Corp_Id=99&Sid=<%=Sid%>';
}
//-------------------��Ŀ����-----------------------
function doProject_Info()
{	
	window.parent.frames.mFrame.location = 'Admin_Project_Info.do?Cmd=0&Sid=<%=Sid%>';
}
//--------------------�ܾ�����----------------------
function doGIS_GJ()
{
	window.parent.frames.mFrame.location = 'Map_GJ.jsp?pProject_Id=1001&Sid=<%=Sid%>';
}
function doGJ_Info()
{
	window.parent.frames.mFrame.location = 'Admin_DevGJ_Info.do?Cmd=0&Sid=<%=Sid%>';
}
function doGX_Info()
{
	window.parent.frames.mFrame.location = 'Admin_DevGX_Info.do?Cmd=0&Sid=<%=Sid%>';
}
function doGJ_Load()
{	
	window.parent.frames.mFrame.location = 'GJ_Excel.jsp?Sid=<%=Sid%>';
}
function doEquip_Info()
{	
	window.parent.frames.mFrame.location = 'Admin_Equipment_Info.do?Sid=<%=Sid%>';
}

function doDiaphic()
{	
	window.parent.frames.mFrame.location = "Diapgic.do?Sid=<%=Sid%>&Cmd=4&Start_Id=HCBWJ002&End_Id=HCBWJ003&Id=HCBWG003";
}
</SCRIPT>
</html>