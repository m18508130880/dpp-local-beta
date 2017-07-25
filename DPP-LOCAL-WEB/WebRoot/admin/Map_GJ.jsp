<%@ page contentType="text/html; charset=gb2312"%>
<%@ page import="java.util.*"%>
<%@ page import="bean.*"%>
<%@ page import="util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>�ܾ���ѯ</title>
<meta http-equiv="x-ua-compatible" content="ie=7"/>
<link type="text/css" href="../skin/css/style.css" rel="stylesheet"/>
<script type='text/javascript' src='../skin/js/util.js' charset='gb2312'></script>
<script type='text/javascript' src='../skin/js/browser.js' charset='gb2312'></script>
<script src="http://api.map.baidu.com/api?v=1.2&services=true" type="text/javascript"></script>
<!--Zdialog-->
<script type='text/javascript' src='../skin/js/zDrag.js'   charset='gb2312'></script>
<script type='text/javascript' src='../skin/js/zDialog.js' charset='gb2312'></script>


<script type="text/javascript" src="../easyui/jquery.min.js"></script>

<script type="text/javascript" src="../easyui/jquery.easyui.min.js"></script>


<!--BanRightClick-->
<script language=javascript> document.oncontextmenu=function(){window.event.returnValue=false;};</script>

<style>
	html{height:100%}
	body{height:100%; margin:0px; padding:0px}
	#container{height:100%}
  html,body{width:100%; height:100%; margin:0; padding:0;}/*���뽫���������һ���߶�*/
  .mesWindow{border:#C7C5C6 1px solid;background:#CADFFF;}
  .mesWindowTop{background:#3ea3f9;padding:5px;margin:0;font-weight:bold;text-align:left;font-size:12px; clear:both; line-height:1.5em; position:relative; clear:both;}
  .mesWindowTop span{ position:absolute; right:5px; top:3px;}
  .mesWindowContent{margin:4px;font-size:12px; clear:both;}
  .mesWindow .close{height:15px;width:28px; cursor:pointer;text-decoration:underline;background:#fff}

  #news_info
  {

  }
</style>
</head>
<%
	
	String Sid = CommUtil.StrToGB2312(request.getParameter("Sid"));
	String pProject_Id = CommUtil.StrToGB2312(request.getParameter("pProject_Id"));
	String Func_Type_Id = CommUtil.StrToGB2312(request.getParameter("Func_Type_Id"));
	ArrayList Map_GJ = (ArrayList)session.getAttribute("Map_GJ_" + Sid);
	UserInfoBean UserInfo    = (UserInfoBean)session.getAttribute("UserInfo_" + Sid);	
	CurrStatus currStatus    = (CurrStatus)session.getAttribute("CurrStatus_" + Sid);
	ArrayList  Project_Info  = (ArrayList)session.getAttribute("Project_Info_" + Sid);
	
  int sn = 0;
	//��ʼ������
	double Longitude = 120.201967;
	double Latitude  = 30.276438;
	int MapLev = 18;
	
	if(Project_Info != null && pProject_Id != null ){
			Iterator iterator = Project_Info.iterator();
			while(iterator.hasNext()){
					ProjectInfoBean sBean = (ProjectInfoBean)iterator.next();
				  if(sBean.getId().equals(pProject_Id)){
				     Longitude	= 	Float.parseFloat(sBean.getLongitude());
				     Latitude  	= 	Float.parseFloat(sBean.getLatitude());
				     MapLev     =   Integer.parseInt(sBean.getMapLev());
				  }
			}
	 }	

%>
<body style="background:#CADFFF">
	<div id="container">	</div>
	<!-- ��ҳ�� -->
		<div id='news_info' style='position:absolute;width:200px;height:22px;right:0px;top:0px;background-color:#FFFFFF;overflow:auto;'>
		  <form  name="gj_class" >
		  	<select id="Project_Select" name="Project_Select" style="width:48%;height:20px" onChange="doReresh()"> 									  
							 <%	
 	                if(Project_Info != null){
		  								Iterator iterator = Project_Info.iterator();
											while(iterator.hasNext()){
											ProjectInfoBean statBean = (ProjectInfoBean)iterator.next();
										  String Pro_Id = statBean.getId();
											String Pro_Name = statBean.getCName();										
							 %>
								  <option value="<%=Pro_Id%>" <%=pProject_Id.equals(Pro_Id)?"selected":""%>> <%=Pro_Name%></option>
							 <%
		    						 }
									}
							 %>
				</select>
				<select id="GJ_Select" name="GJ_Select" style="width:48%;height:20px" onChange="doReresh()"> 									  
						<option value="J"   <%=Func_Type_Id.equals("J")?"selected":""%>   >�����ܵ�ͼ</option>		
					  <option value="YJ"  <%=Func_Type_Id.equals("YJ")?"selected":""%>  >��ˮ����ͼ</option>
					  <option value="WJ"  <%=Func_Type_Id.equals("WJ")?"selected":""%>  >��ˮ����ͼ</option>
				</select>
		  </form>
	  </div>
	<div id='menu_info' style='position:absolute;width:16px;height:100%;right:0px;top:0px;filter:alpha(Opacity=80);-moz-opacity:0.5;opacity:0.5;background-color:#bbbdbb;'>
		<img id='news_img' src='../skin/images/map2close.gif' style='width:16px;height:16px;cursor:hand;' title='����' onclick='doOpen()'>

	</div>
</body>

<SCRIPT LANGUAGE=javascript>
	
	function doOpen()
	{

		if(document.getElementById('news_info').style.display == '')
		{
			document.getElementById('news_img').src = '../skin/images/map2open.gif';
			document.getElementById('news_img').title = '����';
			document.getElementById('news_info').style.display = 'none';

		}
		else
		{

			document.getElementById('news_img').src = '../skin/images/map2close.gif';
			document.getElementById('news_img').title = 'չ��';
			document.getElementById('news_info').style.display = '';

		}
	}

	
//������
if(1 == fBrowserRedirect() || 2 == fBrowserRedirect())
{
	window.addEventListener('onorientationchange' in window ? 'orientationchange' : 'resize', setHeight, false);
	setHeight();
}
function setHeight()
{
	document.getElementById('container').style.height = document.body.offsetHeight + 'px';
}

//�����ͼ
	var map = new BMap.Map("container");                        //������ͼʵ��
	//map.setMapType(BMAP_HYBRID_MAP);                          //Ĭ������Ϊ���ǡ�·��һ��
	point = new BMap.Point(<%=Longitude%>, <%=Latitude%>);      //�������ĵ����꣬Ĭ��Ϊ��һ����ҵ
	map.centerAndZoom(point, <%=MapLev%>);                      //��ʼ����ͼ���������ĵ�����͵�ͼ����
	map.addControl(new BMap.NavigationControl());               //���һ��ƽ�����ſؼ���λ�ÿ�ƫ�ơ���״�ɸı�
	map.addControl(new BMap.ScaleControl());                    //���һ�������߿ؼ���λ�ÿ�ƫ��[var opts = {offset: new BMap.Size(150, 5)};map.addControl(new BMap.ScaleControl(opts));]
	map.addControl(new BMap.OverviewMapControl());              //���һ������ͼ�ؼ���λ�ÿ�ƫ��
	//map.addControl(new BMap.MapTypeControl());                //��ӵ�ͼ���ͱ任(��ͼ-����-��ά)��λ�ÿ�ƫ��
	map.enableScrollWheelZoom();                                //���ù��ַŴ���С
	
	var gjArray = new Array();
	function gjGet(arrPerson,objPropery,objValue)
	{
   return $.grep(arrPerson, function(cur,i){
   				if(objValue == cur[objPropery])
          	return cur[objPropery];
       });
	}
	

//1.��ӵ�ͼ�һ���ӱ�ע
map.addEventListener("rightclick", function(e)
{
 	doRightClick(e);
});

//2.��Ӷ����עͼ��
function addGJMarker(point, pCorp_Id, pCName, pX, pY, pType)
{
	
	var pIcon  = (((pCorp_Id.indexOf("WJ")) > 0)?'../skin/images/map_gj_Red.gif':'../skin/images/map_gj_blue.gif');
	var myIcon = new BMap.Icon(pIcon, new BMap.Size(pX, pY));
 	var marker = new BMap.Marker(point, {icon: myIcon});
 	var myLabel= new BMap.Label(pCName, {offset:new BMap.Size(0, pY)});
 	
 	myLabel.setStyle
 	({
 		fontSize:"11px",
 		font:"bold 10pt/12pt",
 		border:"0px",
 		color:"black",
 		textAlign:"center",
 		background:"yellow",
 		cursor:"pointer"
 	});
 	/*if(pType != 0)
 	{
 		marker.setLabel(myLabel);//���ֱ��
 	}	*/
 	map.addOverlay(marker);
 	
  //���
 	marker.addEventListener("click", function()
 	{
 		doGJDefence(pCorp_Id, pType);
	});
	
	//��ק 
	marker.enableDragging(); 
	marker.addEventListener("dragend", function(e)
	{  
		doDragging(pCorp_Id, e.point.lng, e.point.lat, pType);  	
	});
	
}

function addGXMarker(polyline,gxId)
{
	 map.addOverlay(polyline);
	 polyline.addEventListener("click", function()
 	{
 		doGXDefence(gxId);
	});
} 
	
//�ܾ�״̬���
var reqRealGJ = null;
function RealGJStatus()
{
	if(window.XMLHttpRequest)
  {
    reqRealGJ = new XMLHttpRequest();
  }
	else if(window.ActiveXObject) //��IE6.0��5.5
	{
    reqRealGJ = new ActiveXObject('Microsoft.XMLHTTP');
  }
	reqRealGJ.onreadystatechange = function(){
	  var state = reqRealGJ.readyState;
	  if(state == 4)
	  {
	    if(reqRealGJ.status == 200)
	    {
	      var Resp = reqRealGJ.responseText;
	      if(null != Resp && Resp.length >= 4 && Resp.substring(0,4) == '0000')
	      {
	      	//1.ɾ��
					map.clearOverlays();      	
	      	//2.���
	      	var list = Resp.substring(4).split(";");
	      	for(var i=0; i<list.length && list[i].length>0; i++)
	      	{
	      			
	      		  var sublist = list[i].split("|");
	      		  if(sublist[0].indexOf(document.getElementById("GJ_Select").value)< 0){
      		  	 continue;
      		    }
	      		  var point = new BMap.Point(sublist[1], sublist[2]);  
	      		  var gjObj = new Object();

	      		  gjObj.tId = sublist[0];
	      		  gjObj.tPoint = point;
	      		  gjArray.push(gjObj);
    		  
	      			var pName = 'ˮλ : ';
						  addGJMarker(point, sublist[0], pName+sublist[4], '10', '10', sublist[3]);
							//;TSGYJ005-3|TSGYJ005-2|120.203581|30.276107|0|0;
	      	}
	      }
	    }
	  }
	};
	var url = "Admin_ToPo_GJ.do?Cmd=21&Id="+document.getElementById("Project_Select").value+"&Sid=<%=Sid%>&currtime="+new Date();
	reqRealGJ.open('POST',url,false);
	reqRealGJ.send(null);
}

//����״̬���
var reqRealGX = null;
function RealGXStatus()
{
	if(window.XMLHttpRequest)
  {
    reqRealGX = new XMLHttpRequest();
  }
	else if(window.ActiveXObject) //��IE6.0��5.5
	{
    reqRealGX = new ActiveXObject('Microsoft.XMLHTTP');
  }
	reqRealGX.onreadystatechange = function(){
	  var state = reqRealGX.readyState;
	  if(state == 4)
	  {
	    if(reqRealGX.status == 200)
	    {
	      var Resp = reqRealGX.responseText;
	      if(null != Resp && Resp.length >= 4 && Resp.substring(0,4) == '0000')
	      {

	      	//2.���
	      	var list = Resp.substring(4).split(";");
	      	for(var i=0; i<list.length && list[i].length>0; i++)
	      	{
	      		var sublist = list[i].split("|");
	      		if(sublist[1].indexOf(document.getElementById("GJ_Select").value)< 0){
      		  	 continue;
      		  }
      		  var gjStartId = sublist[0];
      		  var gjStart = gjGet(gjArray, "tId", gjStartId);
      		  var gxId = "";
      		  var polyline = null;
      		  if(sublist[1].indexOf("WJ") > 0)
      		  {
      		  	var gjEndId = sublist[1];
	      		  var gjEnd   = gjGet(gjArray, "tId", gjEndId);
	      		  if(null == gjStart[0] || null == gjEnd[0]){continue;}
						  polyline = new BMap.Polyline([gjStart[0].tPoint, gjEnd[0].tPoint], {strokeColor:"red", strokeWeight:4, strokeOpacity:0.8});
						  gxId = sublist[1].replace('WJ', 'WG');
      		  }
      		  else if(sublist[1].indexOf("YJ") > 0)
      		  {
      		  	var gjEndId = sublist[1];
	      		  var gjEnd   = gjGet(gjArray, "tId", gjEndId);
	      		  if(null == gjStart[0] || null == gjEnd[0]){continue;}
							polyline = new BMap.Polyline([gjStart[0].tPoint, gjEnd[0].tPoint], {strokeColor:"blue", strokeWeight:4, strokeOpacity:0.8});
							gxId = sublist[1].replace('YJ', 'YG');
      		  }
      		  
      		  addGXMarker(polyline,gxId);
	      	}
	      }
	    }
	  }
	};
	var url = "Admin_ToPo_GX.do?Cmd=21&Id="+document.getElementById("Project_Select").value+"&Sid=<%=Sid%>&currtime="+new Date();
	reqRealGX.open('POST',url,false);
	reqRealGX.send(null);
}
RealGJStatus()
RealGXStatus()


//�ҵ���¼�
var reqUnMarke = null;
function doRightClick(e)
{
	//��ȡδ���  XMLHttpRequest ���������ں�̨��������������ݡ�
	if(window.XMLHttpRequest)
  { // code for all new browsers
		reqUnMarke = new XMLHttpRequest();
	}
	else if(window.ActiveXObject)
	{// code for IE5 and IE6
		reqUnMarke = new ActiveXObject("Microsoft.XMLHTTP");
	}
	reqUnMarke.onreadystatechange = function()
	{
	  var state = reqUnMarke.readyState;
	  if(state == 4)
	  {// 4 = "loaded"
	    if(reqUnMarke.status == 200)
	    {// 200 = OK
	      var Resp = reqUnMarke.responseText;
	      if(null != Resp && Resp.substring(0,4) == '0000')
	      {
	      	//վ��
	      	var list = Resp.substring(4).split(';');
					var content = "<select id='Id' name='Id' style='width:220px;height:20px;'>";
	      	for(var i=0; i<list.length && list[i].length>0; i++)
	      	{
	      		content += "<option value='"+ list[i] +"'>"+ list[i] +"</option>";
	      	}
					content += "</select>";
					content += "<input type='button' value='��ע�ܾ�' onClick=\"doAddMarke('0', "+e.point.lng+", "+e.point.lat+")\">";
					var opts = 
					{
					  width : 350, // ��Ϣ���ڿ��  
					  height: 60,  // ��Ϣ���ڸ߶�  
					  title : ""   // ��Ϣ���ڱ���
					}
					var infoWindow = new BMap.InfoWindow(content, opts);//������Ϣ���ڶ���  
					map.openInfoWindow(infoWindow, e.point);            //����Ϣ����
	      }  
	      else
	      {
	    		return;
	      }   
	    }
	    else
	    {
	    	return;
	    }
	  }
	};
	var url = "Admin_ToPo_GJ.do?Cmd=23&Sid=<%=Sid%>&currtime="+new Date();
	reqUnMarke.open("POST",url,true);
	reqUnMarke.send(null);
	return true;
}

//��ӱ�ע
var reqAdd = null;
function doAddMarke(pType, Lng, Lat)
{
	if(document.getElementById('Id').value.length < 1)
	{
		alert('��ѡ��Ҫ��ע��վ��!');
		return;
	}
	var Id = document.getElementById('Id').value;
	if(confirm('ȷ����ӱ�ע?'))
	{
		if(window.XMLHttpRequest)
	  {
			reqAdd = new XMLHttpRequest();
		}
		else if(window.ActiveXObject)
		{
			reqAdd = new ActiveXObject("Microsoft.XMLHTTP");
		}
		reqAdd.onreadystatechange = function()
		{
		  var state = reqAdd.readyState;
		  if(state == 4)
		  {
		    if(reqAdd.status == 200)
		    {
		      var Resp = reqAdd.responseText;
		      if(null != Resp && Resp.substring(0,4) == '0000')
		      {
		      	map.closeInfoWindow();
		      	var point = new BMap.Point(Lng, Lat);
						addGJMarker(point, Id, Id, '10', '10', pType);
		      	alert('��ӱ�ע�ɹ�!');
		    		return;
		      }
		      else
		      {
		      	alert('��ӱ�עʧ��!');
		    		return;
		      }
		    }
		    else
		    {
		    	alert('��ӱ�עʧ��!');
		    	return;
		    }
		  }
		};
		var url = "Admin_Drag_GJ.do?Cmd=17&Sid=<%=Sid%>&Id="+Id+"&Longitude="+Lng+"&Latitude="+Lat+"&currtime="+new Date();
		reqAdd.open("POST",url,true);
		reqAdd.send(null);
		return true;
	}
}

//��ק������½ӿ�
var reqDrg = null;
function doDragging(pId, pLng, pLat, pType)
{
	if(confirm('ͬ�����µ�ǰվ������?'))
	{
		if(window.XMLHttpRequest)
	  {
			reqDrg = new XMLHttpRequest();
		}
		else if(window.ActiveXObject)
		{
			reqDrg = new ActiveXObject("Microsoft.XMLHTTP");
		}
		reqDrg.onreadystatechange = function()
		{
		  var state = reqDrg.readyState;
		  if(state == 4)
		  {
		    if(reqDrg.status == 200)
		    {
		      var Resp = reqDrg.responseText;
		      if(null != Resp && Resp.substring(0,4) == '0000')
		      {
		      	alert('����ͬ�����³ɹ�!');
		    		return;
		      }  
		      else
		      {
		      	alert('����ͬ������ʧ��!');
		    		return;
		      }   
		    }
		    else
		    {
		    	alert('����ͬ������ʧ��!');
		    	return;
		    }
		  }
		};
		var url = "Admin_Drag_GJ.do?Cmd=15&Sid=<%=Sid%>&Id="+pId+"&Longitude="+pLng+"&Latitude="+pLat+"&currtime="+new Date();
		reqDrg.open("POST",url,true);
		reqDrg.send(null);
		return true;
	}
}

//�����鿴�ܾ��ӿ�
function doGJDefence(pId, pType)
{
	
	var Pdiag = new Dialog();
	Pdiag.Top = "50%";
	Pdiag.Width = 160;
	Pdiag.Height = 135;
	Pdiag.Title = "�ܾ�����";
	Pdiag.URL = "Admin_DevGJ_Info.do?Cmd=3&Sid=<%=Sid%>&Id="+pId+"&Type="+pType;
	Pdiag.CancelEvent=function()
	{
		Pdiag.close();	//�رմ���
		//RealGJStatus();		//ҳ��ˢ��
	};
	Pdiag.show();
}
//�����鿴���߽ӿ�
function doGXDefence(pId)
{
	
	var Pdiag = new Dialog();
	Pdiag.Top = "50%";
	Pdiag.Width = 160;
	Pdiag.Height = 135;
	Pdiag.Title = "��������";
	Pdiag.URL = "Admin_DevGX_Info.do?Cmd=3&Sid=<%=Sid%>&Id="+pId;
	Pdiag.CancelEvent=function()
	{
		Pdiag.close();	//�رմ���
		//RealGJStatus();		//ҳ��ˢ��
	};
	Pdiag.show();
}

//ɾ����ע�ӿ�
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
		      if(null != Resp && Resp.substring(0,4) == '0000')
		      {
		      	closeWindow();
		      	map.clearOverlays();
		      	RealGJStatus();
		      	alert('ɾ����ע�ɹ�!');
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

function doReresh()
{
	this.location = "Map_GJ.jsp?pProject_Id="+document.getElementById("Project_Select").value+"&Func_Type_Id="+document.getElementById("GJ_Select").value+"&Sid=<%=Sid%>";
}

</SCRIPT>
</html>