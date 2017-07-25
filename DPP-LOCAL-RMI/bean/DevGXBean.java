package bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jspsmart.upload.SmartUpload;

import net.sf.json.JSONArray;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import rmi.Rmi;
import rmi.RmiBean;
import util.*;

public class DevGXBean extends RmiBean
{	
	public final static long serialVersionUID =RmiBean.RMI_DEVGX;
	public long getClassId()
	{
		return serialVersionUID;
	}
	
	public DevGXBean()
	{
		super.className = "DevGXBean";
	}
	
	public void ExecCmd(HttpServletRequest request, HttpServletResponse response, Rmi pRmi, boolean pFromZone) throws ServletException, IOException
	{
		getHtmlData(request);
		currStatus = (CurrStatus)request.getSession().getAttribute("CurrStatus_" + Sid);
		currStatus.getHtmlData(request, pFromZone);

		switch(currStatus.getCmd())
		{
			case 12://ɾ��
			case 11://�༭	
				msgBean = pRmi.RmiExec(currStatus.getCmd(), this, 0, 25);  
				currStatus.setResult(MsgBean.GetResult(msgBean.getStatus()));
			case 0://admin���߷�ҳ��ѯ
				msgBean = pRmi.RmiExec(0, this,  currStatus.getCurrPage(), 25);
				currStatus.setTotalRecord(msgBean.getCount());
				request.getSession().setAttribute("Dev_GX_List_" + Sid, (Object)msgBean.getMsg());
				currStatus.setJsp("Dev_GX.jsp?Sid=" + Sid);
				break;
			case 1://User�����ҳ��ѯ
				msgBean = pRmi.RmiExec(1, this,  currStatus.getCurrPage(), 25);
				currStatus.setTotalRecord(msgBean.getCount());
				request.getSession().setAttribute("Dev_GX_" + Sid, (Object)msgBean.getMsg());
				currStatus.setJsp("Dev_GX.jsp?Sid=" + Sid);
				break;
			
			case 3://��ѯ����
				msgBean = pRmi.RmiExec(currStatus.getCmd(), this, 0, 25);
				request.getSession().setAttribute("One_GX_" + Sid, (DevGXBean)((ArrayList<?>)msgBean.getMsg()).get(0));				
				currStatus.setJsp("One_GX.jsp?Sid=" + Sid);
				break;
			case 5://admin ��ѯ(�༭)
				msgBean = pRmi.RmiExec(currStatus.getCmd(), this, 0, 25);
				request.getSession().setAttribute("Dev_GX_" + Sid, (DevGXBean)((ArrayList<?>)msgBean.getMsg()).get(0));				
				currStatus.setJsp("Dev_GX_Edit.jsp?Sid=" + Sid);
				break;
			case 4://����ͼ
				msgBean = pRmi.RmiExec(currStatus.getCmd(), this, 0, 25);
				request.getSession().setAttribute("User_Graph_Cut_GX_" + Sid, (Object)msgBean.getMsg());				
				DevGJBean tmpGJBean = new DevGJBean();
				tmpGJBean.setProject_Id(Project_Id);
				tmpGJBean.setSubsys_Id(Id.substring(2, 5));
				msgBean = pRmi.RmiExec(5, tmpGJBean, 0, 25);
				request.getSession().setAttribute("User_Graph_Cut_GJ_" + Sid, (Object)msgBean.getMsg());
				currStatus.setJsp("User_Graph_Cut.jsp?Sid=" + Sid + "&Id=" + Id);
				break;
		}
		
		request.getSession().setAttribute("CurrStatus_" + Sid, currStatus);
	   	response.sendRedirect(currStatus.getJsp());
	}
	
	
	public void GXSuggest(HttpServletRequest request, HttpServletResponse response, Rmi pRmi, boolean pFromZone) throws ServletException, IOException
	{
		PrintWriter output = null;
	    try
	    {
	    	getHtmlData(request);
	    	currStatus = (CurrStatus)request.getSession().getAttribute("CurrStatus_" + Sid);
			currStatus.getHtmlData(request, pFromZone);
	    	
	    	List<Object> CData = new ArrayList<Object>();
    		//����Level���ҹ�Ͻ��վ��
			msgBean = pRmi.RmiExec(0, this, 0, 25);
			if(msgBean.getStatus() == MsgBean.STA_SUCCESS)
			{
				if(null != msgBean.getMsg())
				{
					ArrayList<?> msgList = (ArrayList<?>)msgBean.getMsg();
					if(msgList.size() > 0)
					{						
						Iterator<?> iterator = msgList.iterator();
						while(iterator.hasNext())
						{
							DevGXBean devGXBean = (DevGXBean)iterator.next();
							JsonGXBean RealJson = new JsonGXBean();
							RealJson.setId(devGXBean.getId());
							RealJson.setText(devGXBean.getId());
							CData.add(RealJson);
						}
					}
				}
			}	
			
			JSONArray jsonObj = JSONArray.fromObject(CData);
	    	output = response.getWriter();
	    	output.write(jsonObj.toString());
	    	output.flush();	    	
	    	System.out.println(jsonObj.toString());
	    }
	    catch (IOException e)
	    {
	    	e.printStackTrace();
	    }
	    finally
	    {
	    	if(null != output){
	    		output.close();
	    	}
	    }
	}	
	
	//��ȡ״̬RealStatus��doDefence��doRightClick
	public void ToPo(HttpServletRequest request, HttpServletResponse response, Rmi pRmi, boolean pFromZone) throws ServletException, IOException
	{
		getHtmlData(request);
		currStatus = (CurrStatus)request.getSession().getAttribute("CurrStatus_" + Sid);
		currStatus.getHtmlData(request, pFromZone);
		
		PrintWriter outprint = response.getWriter();
		String Resp = "9999";
		
		msgBean = pRmi.RmiExec(currStatus.getCmd(), this, 0, 25);
		if(msgBean.getStatus() == MsgBean.STA_SUCCESS)
		{
			Resp = ((String)msgBean.getMsg());
		}
		
		request.getSession().setAttribute("CurrStatus_" + Sid, currStatus);
		outprint.write(Resp);
	}
	
	
	/** ����Excel�ĵ�  �����ĵ��еĹܾ���ϸ����  
	 * @param request
	 * @param response
	 * @param pRmi
	 * @param pFromZone
	 * @param pConfig
	 * 
	 */
	public void ImportExcel(HttpServletRequest request, HttpServletResponse response, Rmi pRmi, boolean pFromZone, ServletConfig pConfig) 
	{
		try
		{			
			SmartUpload mySmartUpload = new SmartUpload();
//			mySmartUpload.initialize(pConfig, request, response);
			mySmartUpload.setAllowedFilesList("xls,xlsx,XLS,XLSX,");
			mySmartUpload.upload();
								
			Sid = mySmartUpload.getRequest().getParameter("Sid");
			currStatus = (CurrStatus)request.getSession().getAttribute("CurrStatus_" + Sid);
			Project_Id = mySmartUpload.getRequest().getParameter("Project_Id");

			if(mySmartUpload.getFiles().getCount() > 0 && mySmartUpload.getFiles().getFile(0).getFilePathName().trim().length() > 0)
			{
				if(mySmartUpload.getFiles().getFile(0).getSize()/1024 <= 3072)//���3M
				{		
					String FileSaveRoute = "/www/DPP-LOCAL/DPP-LOCAL-WEB/files/upfiles/";										
					//�ϴ������ĵ�			
					com.jspsmart.upload.File myFile = mySmartUpload.getFiles().getFile(0);		
					String File_Name = new SimpleDateFormat("yyyyMMdd").format(new Date()) + CommUtil.Randon()+ "." + myFile.getFileExt();			
					myFile.saveAs(FileSaveRoute + File_Name);						
					//¼�����ݿ�
					InputStream is = new FileInputStream(FileSaveRoute + File_Name);					
					Workbook rwb = Workbook.getWorkbook(is);					
					Sheet rs = rwb.getSheet(0);					
				    int rsRows = rs.getRows();  //excel����е������������Ƿ��б߿�
				    int succCnt = 0;	
				    int tmpCnt = 0;

				    //������ʼ��
				    int rowStart = 1;
				    //ѭ����ʼ
				    for(int i=rowStart; i<rsRows; i++)
				    {				    	
			    		String id = rs.getCell(1, i).getContents().trim(); 
			    		if(8 > id.length())
			    			continue;
			    		
			    		tmpCnt ++;
			    		String diameter = rs.getCell(2, i).getContents().trim(); 
			    		String length = rs.getCell(3, i).getContents().trim(); 
			    		String startId = rs.getCell(4, i).getContents().trim(); 
			    		String endId = rs.getCell(5, i).getContents().trim(); 
			    		String startHeight = rs.getCell(6, i).getContents().trim(); 
			    		String endHeight = rs.getCell(7, i).getContents().trim();
			    		String material = rs.getCell(8, i).getContents().trim(); 
			    		String buriedYear = rs.getCell(9, i).getContents().trim(); 
			    		String data_Lev = rs.getCell(10, i).getContents().trim(); 

			    		this.setId(id.toUpperCase());			    		
			    		this.setDiameter(!CommUtil.isNumeric(diameter)?"0":diameter);
			    		this.setLength(!CommUtil.isNumeric(length)?"0":length);
			    		this.setStart_Id(startId.toUpperCase());
			    		this.setEnd_Id(endId.toUpperCase());
			    		this.setStart_Height(!CommUtil.isNumeric(startHeight)?"0":startHeight);
			    		this.setEnd_Height(!CommUtil.isNumeric(endHeight)?"0":endHeight);
			    		this.setMaterial(material);
			    		this.setBuried_Year(buriedYear);
			    		this.setData_Lev(data_Lev);
			    		
			    		this.setProject_Id(Project_Id);
			    			    		
			    		//�����ύ
			    		msgBean = pRmi.RmiExec(10, this, 0, 25);
				    	if(msgBean.getStatus() == MsgBean.STA_SUCCESS)
						{
				    		succCnt ++;
						}				    	
				    }
				    currStatus.setResult("�ɹ�����[" + String.valueOf(succCnt) + "/" + String.valueOf(tmpCnt) + "]��");
				}
				else
				{
					currStatus.setResult("�ĵ��ϴ�ʧ�ܣ��ĵ����󣬱���С��3M!");
				}				
			}
			
			currStatus.setJsp("Import_Excel.jsp?Sid=" + Sid + "&Project_Id=" + Project_Id);
			request.getSession().setAttribute("CurrStatus_" + Sid, currStatus);	
		   	response.sendRedirect(currStatus.getJsp());
		}
		catch(Exception exp)
		{
			exp.printStackTrace();
		}
	}
	
	/** �����ܶ������б�
	 * @param request
	 * @param response
	 * @param pRmi
	 * @param pFromZone
	 * @param pConfig
	 * 
	 */
	public void XLQRExcel(HttpServletRequest request,HttpServletResponse response, Rmi pRmi, boolean pFromZone) {
		try {
			getHtmlData(request);
			currStatus = (CurrStatus) request.getSession().getAttribute("CurrStatus_" + Sid);
			currStatus.getHtmlData(request, pFromZone);
			
			SimpleDateFormat SimFormat = new SimpleDateFormat("yyyyMMddHHmmss");

			String SheetName = "������Ϣ��";
			String UPLOAD_NAME = SimFormat.format(new Date());
			System.out.println("SheetName [" + SheetName + "]" );
			msgBean = pRmi.RmiExec(0, this, 0, 25);
			ArrayList<?> gx_List = (ArrayList<?>) msgBean.getMsg();
			int row_Index = 0;
			Label cell = null;
			if (null != gx_List) {
				WritableWorkbook book = Workbook.createWorkbook(new File(UPLOAD_PATH + UPLOAD_NAME + ".xls"));
				// ������Ϊ"��һҳ"�Ĺ���������0��ʾ���ǵ�һҳ
				WritableSheet sheet = book.createSheet(SheetName, 0);
				
				// �����ʽ1
				WritableFont wf = new WritableFont(WritableFont.createFont("normal"), 14,WritableFont.BOLD, false);
				WritableCellFormat font1 = new WritableCellFormat(wf);
				// wf.setColour(Colour.BLACK);//������ɫ
				font1.setAlignment(Alignment.CENTRE);// ���þ���
				font1.setVerticalAlignment(VerticalAlignment.CENTRE); //����Ϊ��ֱ����
				font1.setBorder(Border.ALL, BorderLineStyle.THIN);//���ñ߿���
				
				// �����ʽ2
				WritableFont wf2 = new WritableFont(WritableFont.createFont("normal"), 10,WritableFont.NO_BOLD, false);
				WritableCellFormat font2 = new WritableCellFormat(wf2);
				wf2.setColour(Colour.BLACK);// ������ɫ
				font2.setAlignment(Alignment.CENTRE);// ���þ���
				font2.setVerticalAlignment(VerticalAlignment.CENTRE); //����Ϊ��ֱ����
				font2.setBorder(Border.ALL, BorderLineStyle.THIN);// ���ñ߿���
				
//				// �����ʽ3
//				WritableFont wf3 = new WritableFont(WritableFont.createFont("normal"), 10,WritableFont.BOLD, false);
//				WritableCellFormat font3 = new WritableCellFormat(wf3);
//				font3.setBorder(Border.ALL, BorderLineStyle.THIN);//���ñ߿���
//				
//				// �����ʽ4
//				WritableFont wf4 = new WritableFont(WritableFont.createFont("normal"), 10,WritableFont.BOLD, false);
//				WritableCellFormat font4 = new WritableCellFormat(wf4);
//				wf4.setColour(Colour.BLACK);// ������ɫ
//				font4.setAlignment(Alignment.CENTRE);// ���þ���
//				font4.setBorder(Border.ALL, BorderLineStyle.THIN);// ���ñ߿���
//				font4.setBackground(jxl.format.Colour.TURQUOISE);// ���õ�Ԫ��ı�����ɫ

				sheet.setRowView(row_Index, 450);
				sheet.setColumnView(row_Index, 25);
				cell=new Label(0,0,"����",font1);   
			    sheet.addCell(cell);   
			    cell=new Label(1,0,"ֱ��",font1);  
			    sheet.addCell(cell);   
			    cell=new Label(2,0,"����",font1);  
			    sheet.addCell(cell);   
			    cell=new Label(3,0,"��˹ܾ�",font1);  
			    sheet.addCell(cell);   
			    cell=new Label(4,0,"�ն˹ܾ�",font1);  
			    sheet.addCell(cell);
			    cell=new Label(5,0,"��˵ױ��",font1);  
			    sheet.addCell(cell);
			    cell=new Label(6,0,"�ն˵ױ��",font1);  
			    sheet.addCell(cell);
			    cell=new Label(7,0,"��������",font1);  
			    sheet.addCell(cell); 
			    cell=new Label(8,0,"�������",font1);  
			    sheet.addCell(cell);
			    cell=new Label(9,0,"���ݵȼ�",font1);  
			    sheet.addCell(cell);
			    cell=new Label(10,0,"������Ŀ",font1);  
			    sheet.addCell(cell);  
			    cell=new Label(11,0,"�豸����",font1);  
			    sheet.addCell(cell);  
			    
				
				Iterator<?> gx_iterator = gx_List.iterator();

				while (gx_iterator.hasNext()) {
					DevGXBean devGXBean = (DevGXBean) gx_iterator.next();
					Id = devGXBean.getId();
					Diameter = devGXBean.getDiameter();
					Length = devGXBean.getLength();
					Start_Id = devGXBean.getStart_Id();
					End_Id = devGXBean.getEnd_Id();
					Start_Height = devGXBean.getStart_Height();
					End_Height = devGXBean.getEnd_Height();
					Material = devGXBean.getMaterial();
					Buried_Year = devGXBean.getBuried_Year();
					Data_Lev = "";
					try{
						if(devGXBean.getData_Lev() != null && !devGXBean.getData_Lev().trim().equals("")){
						  	switch(Integer.parseInt(devGXBean.getData_Lev())){
					  		case 1:
					  			Data_Lev ="�˹���ֵ";
					  			break;
					  		case 2:
						  		Data_Lev ="ԭʼ̽��";
						  		break;
					  		case 3:
						  		Data_Lev ="����ͼ����";
						  		break;
					  		case 4:
						  		Data_Lev ="�˹���ֵ�����ֳ�У��";
						  		break;
					  		case 5:
						  		Data_Lev ="ԭʼ̽�⾭������У��";
						  		break;
					  		case 6:
						  		Data_Lev ="��������";
						  		break;
					  		default:
						  		Data_Lev ="����������Ҫ���ģ�";
							  		break;
						  	}
						}
					}catch(Exception e){
					  	Data_Lev ="����������Ҫ���ģ�";
					}finally{
					  	if(Data_Lev == null){
					  		Data_Lev ="";
					  	}
					}
					Curr_Data = devGXBean.getCurr_Data();
					Project_Name = devGXBean.getProject_Name();
					Equip_Name = devGXBean.getEquip_Name();

					row_Index++;
					sheet.setRowView(row_Index, 400);
					sheet.setColumnView(row_Index, 25); // row_Index �п��
					cell = new Label(0, row_Index, Id, font2);
					sheet.addCell(cell);
					cell = new Label(1, row_Index, Diameter, font2);
					sheet.addCell(cell);
					cell = new Label(2, row_Index, Length, font2);
					sheet.addCell(cell);
					cell = new Label(3, row_Index, Start_Id, font2);
					sheet.addCell(cell);
					cell = new Label(4, row_Index, End_Id, font2);
					sheet.addCell(cell);
					cell = new Label(5, row_Index, Start_Height, font2);
					sheet.addCell(cell);
					cell = new Label(6, row_Index, End_Height, font2);
					sheet.addCell(cell);
					cell = new Label(7, row_Index, Material, font2);
					sheet.addCell(cell);
					cell = new Label(8, row_Index, Buried_Year, font2);
					sheet.addCell(cell);
					cell = new Label(9, row_Index, Data_Lev, font2);
					sheet.addCell(cell);
					cell = new Label(10, row_Index, Project_Name, font2);
					sheet.addCell(cell);
					cell = new Label(11, row_Index, Equip_Name, font2);
					sheet.addCell(cell);

				}

				book.write();
				book.close();
				try {
					PrintWriter out = response.getWriter();
					out.print(UPLOAD_NAME);
				} catch (Exception exp) {
					exp.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
		
	public String getSql(int pCmd)
	{  
		String Sql = "";
		switch (pCmd)
		{  
			case 0://��ѯ������&��Ŀ��
				Sql = " select t.id, t.diameter, t.length, t.start_id, t.end_id, t.start_height, t.end_height, t.material, t.buried_year, t.data_lev, t.project_id, t.project_name, t.equip_id, t.equip_name ,round((t.curr_data),2)" +
		    	 	   	  " from view_dev_gx t " +		
		    	 	   	  " where t.id like  '%"+ currStatus.getFunc_Sub_Type_Id() +"%'" +  
		    	 	   	  " and t.project_id = '" + currStatus.getFunc_Project_Id() + "' " +
	 	 		          " order by t.id ";
				break;
		    case 1://��ѯ�������
		    	switch(Integer.parseInt(currStatus.getFunc_Sort_Id()))
				{
					case 1://����ID����
						Sql = " select t.id, t.diameter, t.length, t.start_id, t.end_id, t.start_height, t.end_height, t.material, t.buried_year, t.data_lev, t.project_id, t.project_name, t.equip_id, t.equip_name ,round((t.curr_data),2)" +
				    	 	   	  " from view_dev_gx t " +		
				    	 	   	  " where t.id like  '%"+ currStatus.getFunc_Sub_Type_Id() +"%'" +  
				    	 	   	  " and t.project_id like '" + currStatus.getFunc_Project_Id() + "%' " +
			 	 		          " order by t.id ";
						break;
					case 2://����ֱ������
						Sql = " select t.id, t.diameter, t.length, t.start_id, t.end_id, t.start_height, t.end_height, t.material, t.buried_year, t.data_lev, t.project_id, t.project_name, t.equip_id, t.equip_name ,round((t.curr_data),2)" +
			    	 	   	  " from view_dev_gx t " +		
			    	 	   	  " where t.id like  '%"+ currStatus.getFunc_Sub_Type_Id() +"%'" +  
			    	 	   	  " and t.project_id like '" + currStatus.getFunc_Project_Id() + "%' " +
		 	 		          " order by t.diameter ";
				       break;
				    case 3://���ղ�������
						Sql = " select t.id, t.diameter, t.length, t.start_id, t.end_id, t.start_height, t.end_height, t.material, t.buried_year, t.data_lev, t.project_id, t.project_name, t.equip_id, t.equip_name ,round((t.curr_data),2)" +
			    	 	   	  " from view_dev_gx t " +		
			    	 	   	  " where t.id like '%"+ currStatus.getFunc_Sub_Type_Id() +"%'" +
			    	 	   	  " and t.project_id like '" + currStatus.getFunc_Project_Id() + "%' " +
		 	 		          " order by FIELD(t.material, 'PE', '������') desc";
					   break;
				}
				break;
  
		    case 3://��ѯ(����)
		    case 5:
				Sql = " select t.id, t.diameter, t.length, t.start_id, t.end_id, t.start_height, t.end_height, t.material, t.buried_year, t.data_lev, t.project_id, t.project_name, t.equip_id, t.equip_name ,round((t.curr_data),2)" +
	    	 	   	  " from view_dev_gx t " +		
	    	 	   	  " where t.id = '"+ Id +"'" ;	 		         
			   break;
		    case 4://��ѯ����Ŀ&��ϵͳ��
				Sql = " select t.id, t.diameter, t.length, t.start_id, t.end_id, t.start_height, t.end_height, t.material, t.buried_year, t.data_lev, t.project_id, t.project_name, t.equip_id, t.equip_name ,round((t.curr_data),2)" +
				      " from view_dev_gx t " +	
				      " where t.project_id = '" + Project_Id + "'" + 
				      " and substr(t.id, 3, 3) = '"+ Id.substring(2,5) +"'" +
				      " order by t.id ";
			   break;
		    case 10://���
		    	Sql = " insert into dev_gx(id, diameter, length, start_id, end_id, start_height, end_height, material, buried_year, data_lev, project_id)" +
		    			"values('"+ Id +"', '"+ Diameter +"', '"+ Length +"', '"+ Start_Id +"', '"+ End_Id +"',  '"+ Start_Height +"', '"+ End_Height +"',  '"+Material +"', '"+Buried_Year +"', '"+Data_Lev +"', '"+ Project_Id +"')";
		    	break;	   
		    case 11://�༭
				Sql = " update dev_gx t set t.start_id= '"+ Start_Id + "', t.end_id = '"+ End_Id  + "', t.start_height = '"+ Start_Height + "', t.end_height = '"+ End_Height +"' ,t.diameter= '"+ Diameter + "', t.length = '"+ Length + "', t.buried_year = '"+ Buried_Year + "', t.data_lev = '"+ Data_Lev +"',t.material = '"+ Material + "' " +
					  " where t.id = '"+ Id +"'";
				break;
		    case 12://ɾ��
		    	Sql = " delete from dev_gx where id = '"+ Id +"' ";
		    	break;
	    /*case 14://�յ���ק
				Sql = " update dev_gx t set t.end_longi = '"+ End_Longi +"', t.end_lati = '"+ End_Lati +"' " +
					  " where t.id = '"+ Id +"'";
				break;
				
			case 15://ɾ������ע�ӿ�
				Sql = " update dev_gx t set t.start_sign = '0' " +
				      " where t.id = '"+ Id +"'";
				break;
				
			case 16://ɾ���յ��ע�ӿ�
				Sql = " update dev_gx t set t.end_sign = '0' " +
				      " where t.id = '"+ Id +"'";
				break;
			
			case 17://�������ע�ӿ�
				Sql = " update dev_gx t set t.start_sign = '1', t.start_longi = '"+ Start_Longi +"', t.start_lati = '"+ Start_Lati +"' " +
					  " where t.id = '"+ Id +"'";
				break;
			case 18://�������ע�ӿ�
				Sql = " update dev_gx t set t.end_sign = '1', t.end_longi = '"+ End_Longi +"', t.end_lati = '"+ End_Lati +"' " +
					  " where t.id = '"+ Id +"'";
				break;	*/
				
				
			case 21://��ȡ״̬
				Sql = "{? = call Func_GX_Get('"+ Id +"')}";
				break;

			case 23://��ȡδ��ע��ҵ
				Sql = "{? = call Func_UnMark_GX_Get('')}";
				break;
			case 40://�༭�豸EquipInfo
				Sql = "{call pro_update_dev_gx('" + Equip_Id + "', '" + Equip_Name + "', '" + Id + "')}";
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
			setDiameter(pRs.getString(2));
			setLength(pRs.getString(3));
			setStart_Id(pRs.getString(4));
			setEnd_Id(pRs.getString(5));
			setStart_Height(pRs.getString(6));
			setEnd_Height(pRs.getString(7));
			setMaterial(pRs.getString(8));
			setBuried_Year(pRs.getString(9));
			setData_Lev(pRs.getString(10));
			setProject_Id(pRs.getString(11));
			setProject_Name(pRs.getString(12));
			setEquip_Id(pRs.getString(13));
			setEquip_Name(pRs.getString(14));
			setCurr_Data(pRs.getString(15));
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
			setSid(CommUtil.StrToGB2312(request.getParameter("Sid")));
			setId(CommUtil.StrToGB2312(request.getParameter("Id")));
			setDiameter(CommUtil.StrToGB2312(request.getParameter("Diameter")));
			setLength(CommUtil.StrToGB2312(request.getParameter("Length")));
			setStart_Id(CommUtil.StrToGB2312(request.getParameter("Start_Id")));
			setEnd_Id(CommUtil.StrToGB2312(request.getParameter("End_Id")));
			setStart_Height(CommUtil.StrToGB2312(request.getParameter("Start_Height")));
			setEnd_Height(CommUtil.StrToGB2312(request.getParameter("End_Height")));
			setMaterial(CommUtil.StrToGB2312(request.getParameter("Material")));
			setBuried_Year(CommUtil.StrToGB2312(request.getParameter("Buried_Year")));
			setData_Lev(CommUtil.StrToGB2312(request.getParameter("Data_Lev")));
			setProject_Id(CommUtil.StrToGB2312(request.getParameter("Project_Id")));
			setProject_Name(CommUtil.StrToGB2312(request.getParameter("Project_Name")));
			setEquip_Id(CommUtil.StrToGB2312(request.getParameter("Equip_Id")));
			setEquip_Name(CommUtil.StrToGB2312(request.getParameter("Equip_Name")));
			setSubsys_Id(CommUtil.StrToGB2312(request.getParameter("Subsys_Id")));
			
		}
		catch (Exception Exp)
		{
			Exp.printStackTrace();
		}
		return IsOK;
	}
	
	private String Id;
	private String Diameter;
	private String Length;	
	private String Start_Id;
	private String End_Id;
	private String Start_Height;
	private String End_Height;
	private String Material;
	private String Buried_Year;
	private String Data_Lev;
	
	private String Project_Id;	
	private String Project_Name;	
	private String Equip_Id;	
	private String Equip_Name;
	private String Curr_Data;
	private String Subsys_Id;
	
	
	
	private String Sid;	
	
	public String getCurr_Data() {
		return Curr_Data;
	}

	public void setCurr_Data(String curr_Data) {
		Curr_Data = curr_Data;
	}

	
	
	public String getSubsys_Id() {
		return Subsys_Id;
	}

	public void setSubsys_Id(String subsys_Id) {
		Subsys_Id = subsys_Id;
	}

	public String getStart_Height() {
		return Start_Height;
	}

	public void setStart_Height(String start_Height) {
		Start_Height = start_Height;
	}

	public String getEnd_Height() {
		return End_Height;
	}

	public void setEnd_Height(String end_Height) {
		End_Height = end_Height;
	}


	public String getBuried_Year() {
		return Buried_Year;
	}

	public void setBuried_Year(String buried_Year) {
		Buried_Year = buried_Year;
	}

	public String getData_Lev() {
		return Data_Lev;
	}

	public void setData_Lev(String data_Lev) {
		Data_Lev = data_Lev;
	}

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public String getDiameter() {
		return Diameter;
	}

	public void setDiameter(String diameter) {
		Diameter = diameter;
	}

	public String getLength() {
		return Length;
	}

	public void setLength(String length) {
		Length = length;
	}

	public String getStart_Id() {
		return Start_Id;
	}

	public void setStart_Id(String start_Id) {
		Start_Id = start_Id;
	}

	public String getEnd_Id() {
		return End_Id;
	}

	public void setEnd_Id(String end_Id) {
		End_Id = end_Id;
	}

	public String getMaterial() {
		return Material;
	}

	public void setMaterial(String material) {
		Material = material;
	}

	public String getProject_Id() {
		return Project_Id;
	}

	public void setProject_Id(String project_Id) {
		Project_Id = project_Id;
	}

	public String getProject_Name() {
		return Project_Name;
	}

	public void setProject_Name(String project_Name) {
		Project_Name = project_Name;
	}

	public String getEquip_Id() {
		return Equip_Id;
	}

	public void setEquip_Id(String equip_Id) {
		Equip_Id = equip_Id;
	}

	public String getEquip_Name() {
		return Equip_Name;
	}

	public void setEquip_Name(String equip_Name) {
		Equip_Name = equip_Name;
	}

	public String getSid() {
		return Sid;
	}

	public void setSid(String sid) {
		Sid = sid;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}


