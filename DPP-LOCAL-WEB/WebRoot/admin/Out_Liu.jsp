<%@ page contentType="text/html; charset=gb2312" %>
<%@ page import="java.util.*" %>
<%@ page import="bean.*" %>
<%@ page import="util.*" %>
<%@ page import="java.lang.*" %>
<%@ page import="java.math.*" %>
<%@ page import="java.text.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>ˮλ����ͼ</title>
<link   type="text/css" href="../skin/css/style.css" rel="stylesheet"/>
<script type="text/javascript" src="../skin/js/jquery.min.js"></script>
<script type="text/javascript" src="../skin/js/highcharts.js"></script>
<script type="text/javascript" src="../skin/js/My97DatePicker/WdatePicker.js"></script>
<script type="text/javascript" src="../skin/js/util.js"></script>

</head>
<%
	
	String Sid   = CommUtil.StrToGB2312(request.getParameter("Sid"));
  CurrStatus currStatus = (CurrStatus)session.getAttribute("CurrStatus_" + Sid);
  UserInfoBean UserInfo = (UserInfoBean)session.getAttribute("UserInfo_" + Sid);
	ArrayList User_Manage_Role   = (ArrayList)session.getAttribute("User_Manage_Role_" + Sid);

		
	int BYear = Integer.parseInt(CommUtil.getDate().substring(0,4));
  int BMonth = Integer.parseInt(CommUtil.getDate().substring(5,7));
  int EYear = Integer.parseInt(CommUtil.getDate().substring(0,4));
  int EMonth = Integer.parseInt(CommUtil.getDate().substring(5,7));
  int BWeek = 1;
  int EWeek = 1;
  String BDate = CommUtil.getDate();
  String EDate = CommUtil.getDate();
  
  if(null != (String)session.getAttribute("BYear_" + Sid) && ((String)session.getAttribute("BYear_" + Sid)).trim().length() > 0){BYear = CommUtil.StrToInt((String)session.getAttribute("BYear_" + Sid));}
  if(null != (String)session.getAttribute("BMonth_" + Sid) && ((String)session.getAttribute("BMonth_" + Sid)).trim().length() > 0){BMonth = CommUtil.StrToInt((String)session.getAttribute("BMonth_" + Sid));}
  if(null != (String)session.getAttribute("EYear_" + Sid) && ((String)session.getAttribute("EYear_" + Sid)).trim().length() > 0){EYear = CommUtil.StrToInt((String)session.getAttribute("EYear_" + Sid));}
  if(null != (String)session.getAttribute("EMonth_" + Sid) && ((String)session.getAttribute("EMonth_" + Sid)).trim().length() > 0){EMonth = CommUtil.StrToInt((String)session.getAttribute("EMonth_" + Sid));}
  if(null != (String)session.getAttribute("BWeek_" + Sid)  && ((String)session.getAttribute("BWeek_" + Sid)).trim().length() > 0) {BWeek = CommUtil.StrToInt((String)session.getAttribute("BWeek_" + Sid));}
  if(null != (String)session.getAttribute("EWeek_" + Sid)  && ((String)session.getAttribute("EWeek_" + Sid)).trim().length() > 0) {EWeek = CommUtil.StrToInt((String)session.getAttribute("EWeek_" + Sid));}
  if(null != (String)session.getAttribute("BDate_" + Sid) && ((String)session.getAttribute("BDate_" + Sid)).trim().length() > 0){BDate = (String)session.getAttribute("BDate_" + Sid);}
  if(null != (String)session.getAttribute("EDate_" + Sid) && ((String)session.getAttribute("EDate_" + Sid)).trim().length() > 0){EDate = (String)session.getAttribute("EDate_" + Sid);}
  
%>
<body style='background:#ffffff'>
<form name='Pro_G'  action='Pro_G.do' method='post' target='mFrame'>
<div id='down_bg_2'>
	<table style='margin:auto' border=0 cellPadding=0 cellSpacing=0 bordercolor='#3491D6' borderColorDark='#ffffff' width='100%'>  
	  <tr height='25px' class='sjtop'>
	    <td width='85%' align='left'>
	    	
				</select>
	    	ͼ��:
	      <select name='Func_Sub_Id'  style='width:90px;height:20px' onChange='doSelect()'>
	        <option value='1' <%=(currStatus.getFunc_Sub_Id() == 1 ?"SELECTED":"")%>>����ͼ��</option>	        
	      </select>
	      ģʽ:
	      <select name='Func_Sel_Id' style='width:90px;height:20px' onChange='doSelect()'>
	      	<option value='0' <%=(currStatus.getFunc_Sel_Id() == 0 ?"SELECTED":"")%>>�������</option>
	        <option value='1' <%=(currStatus.getFunc_Sel_Id() == 1 ?"SELECTED":"")%>>���·���</option>
	        <option value='2' <%=(currStatus.getFunc_Sel_Id() == 2 ?"SELECTED":"")%>>���ܷ���</option>
	        <option value='3' <%=(currStatus.getFunc_Sel_Id() == 3 ?"SELECTED":"")%>>���շ���</option>
	      </select>
	      <select id="BYear" name="BYear" style="width:60px;height:20px;">
        <%
        for(int j=2012; j<=2049; j++)
        {
        %>
          <option value="<%=j%>" <%=(BYear == j?"selected":"")%>><%=j%>��</option>
        <%
        }
        %>
        </select>
	      <select id="BMonth" name="BMonth" style="width:50px;height:20px;">
        <%
        for(int k=1; k<13; k++)
        {
       	%>
       		<option value="<%=k%>" <%=(BMonth == k?"selected":"")%>><%=k%>��</option>
       	<%
       	}
       	%>
        </select>
	      <select id="BWeek" name="BWeek" style="width:70px;height:20px;">
        	<option value="1" <%=(BWeek == 1?"selected":"")%>>��һ��</option>
        	<option value="2" <%=(BWeek == 2?"selected":"")%>>�ڶ���</option>
        	<option value="3" <%=(BWeek == 3?"selected":"")%>>������</option>
        	<option value="4" <%=(BWeek == 4?"selected":"")%>>������</option>
        	<option value="5" <%=(BWeek == 5?"selected":"")%>>������</option>
        </select>      
	      <input type='text' id='BDate' name='BDate' style='width:90px;height:18px;' value='<%=BDate%>' onClick='WdatePicker({readOnly:true})' class='Wdate' maxlength='10'>
	      -
	      <select id="EYear" name="EYear" style="width:60px;height:20px;">
        <%
        for(int j=2012; j<=2049; j++)
        {
        %>
          <option value="<%=j%>" <%=(EYear == j?"selected":"")%>><%=j%>��</option>
        <%
        }
        %>
        </select>
	      <select id="EMonth" name="EMonth" style="width:50px;height:20px;">
        <%
        for(int k=1; k<13; k++)
        {
       	%>
       		<option value="<%=k%>" <%=(EMonth == k?"selected":"")%>><%=k%>��</option>
       	<%
       	}
       	%>
        </select>
        <select id="EWeek" name="EWeek" style="width:70px;height:20px;">
        	<option value="1" <%=(EWeek == 1?"selected":"")%>>��һ��</option>
        	<option value="2" <%=(EWeek == 2?"selected":"")%>>�ڶ���</option>
        	<option value="3" <%=(EWeek == 3?"selected":"")%>>������</option>
        	<option value="4" <%=(EWeek == 4?"selected":"")%>>������</option>
        	<option value="5" <%=(EWeek == 5?"selected":"")%>>������</option>
        </select>
        <input type='text' id='EDate' name='EDate' style='width:90px;height:18px;' value='<%=EDate%>' onClick='WdatePicker({readOnly:true})' class='Wdate' maxlength='10'>
	    </td>
      <td width='15%' align='right'>
        <img style='cursor:hand' onClick='doSelect()' src='../skin/images/mini_button_search.gif'>
      </td>
    </tr>  
				<tr height='30' valign='top'>
		    	<td width='100%' align='center' colspan=2>
		      	<div id='container' style='width:100%;height:350px;margin: 0 auto'></div>
		    	</td>
		  	</tr>	
  </table>
	<input name="Cmd"    type="hidden"  value="20">
	<input name="Sid"    type="hidden"  value="<%=Sid%>">
	<input name="Cpm_Id" type="hidden"  value="">
	<input type="button" id="CurrButton"  onClick="doSelect()" style="display:none">
</div>
</form>
</body>
<SCRIPT LANGUAGE=javascript>

switch(parseInt(<%=currStatus.getFunc_Sel_Id()%>))
{
	case 0://�������
			document.getElementById('BYear').style.display = '';
			document.getElementById('BMonth').style.display = 'none';
			document.getElementById('BWeek').style.display = 'none';
			document.getElementById('BDate').style.display = 'none';
			document.getElementById('EYear').style.display = '';
			document.getElementById('EMonth').style.display = 'none';
			document.getElementById('EWeek').style.display = 'none';
			document.getElementById('EDate').style.display = 'none';
		break;
	case 1://���·���
			document.getElementById('BYear').style.display = '';
			document.getElementById('BMonth').style.display = '';
			document.getElementById('BWeek').style.display = 'none';
			document.getElementById('BDate').style.display = 'none';
			document.getElementById('EYear').style.display = '';
			document.getElementById('EMonth').style.display = '';
			document.getElementById('EWeek').style.display = 'none';
			document.getElementById('EDate').style.display = 'none';
		break;
	case 2://���ܷ���
			document.getElementById('BYear').style.display = '';
			document.getElementById('BMonth').style.display = '';
			document.getElementById('BWeek').style.display = '';
			document.getElementById('BDate').style.display = 'none';
			document.getElementById('EYear').style.display = '';
			document.getElementById('EMonth').style.display = '';
			document.getElementById('EWeek').style.display = '';
			document.getElementById('EDate').style.display = 'none';
		break;
	case 3://���շ���
			document.getElementById('BYear').style.display = 'none';
			document.getElementById('BMonth').style.display = 'none';
			document.getElementById('BWeek').style.display = 'none';
			document.getElementById('BDate').style.display = '';
			document.getElementById('EYear').style.display = 'none';
			document.getElementById('EMonth').style.display = 'none';
			document.getElementById('EWeek').style.display = 'none';
			document.getElementById('EDate').style.display = '';
		break;
}

function doSelect()
{
	switch(parseInt(Pro_G.Func_Sel_Id.value))
	{
		case 0://�������(��Ȳ�����12��)
			if((parseInt(Pro_G.EYear.value) - parseInt(Pro_G.BYear.value)) < 0)
			{
				alert('��ֹ�������ڿ�ʼ���!');
				return;
			}
			if((parseInt(Pro_G.EYear.value) - parseInt(Pro_G.BYear.value) + 1) > 12)
			{
				alert('��ȿ�Խ������12��!');
				return;
			}
			break;
		case 1://���·���(��Ȳ�����12��)
			if((parseInt(Pro_G.EYear.value) - parseInt(Pro_G.BYear.value)) < 0)
			{
				alert('��ֹ�·�����ڿ�ʼ�·�!');
				return;
			}
			if((parseInt(Pro_G.EYear.value) - parseInt(Pro_G.BYear.value)) == 0 && (parseInt(Pro_G.EMonth.value) - parseInt(Pro_G.BMonth.value)) < 0)
			{
				alert('��ֹ�·�����ڿ�ʼ�·�!');
				return;
			}
			if((parseInt(Pro_G.EYear.value) - parseInt(Pro_G.BYear.value))*12 + (parseInt(Pro_G.EMonth.value) - parseInt(Pro_G.BMonth.value) + 1) > 12)
			{
				alert('�·ݿ�Խ������12��!');
				return;
			}
			break;
		case 2://���ܷ���(��Ȳ�����12��)
			if((parseInt(Pro_G.EYear.value) - parseInt(Pro_G.BYear.value)) < 0)
			{
				alert('��ֹ������ڿ�ʼ��!');
				return;
			}
			if((parseInt(Pro_G.EYear.value) - parseInt(Pro_G.BYear.value)) == 0 && (parseInt(Pro_G.EMonth.value) - parseInt(Pro_G.BMonth.value)) < 0)
			{
				alert('��ֹ������ڿ�ʼ��!');
				return;
			}
			if((parseInt(Pro_G.EYear.value) - parseInt(Pro_G.BYear.value)) == 0 && (parseInt(Pro_G.EMonth.value) - parseInt(Pro_G.BMonth.value)) == 0 && (parseInt(Pro_G.EWeek.value) - parseInt(Pro_G.BWeek.value)) < 0)
			{
				alert('��ֹ������ڿ�ʼ��!');
				return;
			}
			if((parseInt(Pro_G.EYear.value) - parseInt(Pro_G.BYear.value))*12*5 + (parseInt(Pro_G.EMonth.value) - parseInt(Pro_G.BMonth.value))*5 + (parseInt(Pro_G.EWeek.value) - parseInt(Pro_G.BWeek.value) + 1) > 12)
			{
				alert('�ܿ�Խ������12��!');
				return;
			}
			break;
		case 3://���շ���(��Ȳ�����15��)
			var days = new Date(Pro_G.EDate.value.replace(/-/g, "/")).getTime() - new Date(Pro_G.BDate.value.replace(/-/g, "/")).getTime();
			var dcnt = parseInt(days/(1000*60*60*24));
			if(dcnt < 0)
			{
				alert('��ֹ��������ڿ�ʼ����');
				return;
			}
			if((dcnt + 1) > 15)
			{
				alert('���ڿ�Խ������15��');
				return;
			}
			break;
	}
 // Pro_G.Cpm_Id.value = Pro_G.Func_Cpm_Id.value;
 // Pro_G.submit();
}	
/**
			$(function () 
			{
				var json_serie = [];
				var json_xAxis = [];
				var json_xA		 = [];
				var data_serie = [];
				var data_xAxis = [];
				<%		
					for(int i=0; i<1; i++)
					{						  
				      for(int j=4; j<5; j++)
							{								
								for(int k=0; k<6; k++)
								{							
				%>
		
									data_serie.push(parseFloat('A1'));
									data_xAxis.push('201'+<%=k%>);
				<%
								}
						  }
				%>
						  json_serie.push({'name': '��ˮ������', 'data': data_serie});
						  json_xAxis.push({'categories': data_xAxis});
						  data_serie = [];
						  data_xAxis = [];
				<%
						
					}				
				%>
				
				document.getElementById('container').style.height = document.body.offsetHeight + 'px';
			  var chart;
			  $(document).ready(function() {
			      chart = new Highcharts.Chart({
			          chart: {
			              renderTo: 'container',
			              type: 'line',
			              marginRight: 130,
			              marginBottom: 25
			          },
			          title: 
			          {
				          	<%
										switch(currStatus.getFunc_Sel_Id())
										{
											case 0:
										%>
													text: '���',
										<%
												break;
											case 1:
										%>
													text: '�·�',
										<%
												break;
											case 2:
										%>
													text: '����',
										<%
												break;
											case 3:
										%>
													text: '����',
										<%
												break;
										}
										%>
			              x: -20 //center
			          },
			          subtitle: 
			          {
			              text: '',
			              x: -20
			          },
			          series:json_serie,			                			          
			          xAxis:json_xAxis,
			          yAxis: 
			          {
			              title: {
			                  text: '��ˮ������(CMS)'
			              },
			              plotLines: [{
			                  value: 0,
			                  width: 1,
			                  color: '#808080'
			              }]
			          },
			          tooltip: 
			          {
			              formatter: function() {
			                      return '<b>'+ this.series.name +'</b><br/>'+
			                      this.x +': '+ this.y;
			              }
			          },
			          legend: 
			          {
			              layout: 'vertical',
			              align: 'right',
			              verticalAlign: 'top',
			              x: -10,
			              y: 100,
			              borderWidth: 0
			          }			          
			      });
			  });
			});**/			
		var chart; 						
    $(document).ready(function () { 
        chart = new Highcharts.Chart({ 
            chart: { 
                renderTo: 'container', 
                defaultSeriesType: 'line', 
                marginRight: 130, 
                marginBottom: 25 
            }, 
            title: { 
                text: '���2016',  
                x: -20  
            }, 
            subtitle: { 
                text: '',  
                x: -20 
            }, 
                             
            xAxis: { 
                categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 
            'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']//����x��ı��� 
            }, 
            yAxis: { 
                title: { 
                    text: '��ˮ������(CMS)' //����y��ı��� 
                }, 
                plotLines: [{ 
                    value: 0, 
                    width: 1, 
                    color: '#808080' 
                }] 
            }, 
            tooltip: { 
                formatter: function () { 
                    return '<b>' + this.series.name + '</b><br/>' + 
               this.x + ': ' + this.y ;  //���������ݵ����ʾ��Ϣ�����ǵ�������ʾ��ÿ���ڵ���������ֵʱ�Ͳ������������ʾ��Ϣ 
                } 
            }, 
            legend: { 
                layout: 'vertical', 
                align: 'right', 
                verticalAlign: 'top', 
                x: 0, 
                y: 100, 
                borderWidth: 0 
            }, 
            exporting: { 
                enabled: true,  
                url: "http://localhost:49394/highcharts_export.aspx" //����ͼƬ��URL��Ĭ�ϵ�������Ҫ�����ٷ���վȥ��Ŷ 
            }, 
            plotOptions: { 
                line: { 
                    dataLabels: { 
                        enabled: true //��ʾÿ������ÿ���ڵ���������ֵ 
                    }, 
                    enableMouseTracking: false 
                } 
            }, 
            series: [{ 
            	alert(list[0]);
                name: '��ȫ��û', //ÿ���ߵ����� 
                data: [7.0, 6.9, 9.5, 14.5, 18.2, 21.5, 25.2, 26.5, 23.3, 18.3, 13.9, 9.6]//ÿ���ߵ����� 
            }, { 
                name: '��������', 
                data: [-0.2, 0.8, 5.7, 11.3, 17.0, 22.0, 24.8, 24.1, 20.1, 14.1, 8.6, 2.5] 
            }, { 
                name: '����û', 
                data: [-0.9, 0.6, 3.5, 8.4, 13.5, 17.0, 18.6, 17.9, 14.3, 9.0, 3.9, 1.0] 
            }] 
        });

    }); 		
			
			

</script>
</html>