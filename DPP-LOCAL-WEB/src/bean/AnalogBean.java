package bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import rmi.Rmi;
import util.CommUtil;
import util.CurrStatus;

import com.jspsmart.upload.SmartUpload;

public class AnalogBean
{

	/**
	 * ģ�����ʱ����excel���
	 * 
	 * @param request
	 * @param response
	 * @param pRmi
	 * @param pFromZone
	 * @param pConfig
	 */
	public void ImportData(HttpServletRequest request, HttpServletResponse response, Rmi pRmi, boolean pFromZone, ServletConfig pConfig)
	{
		SmartUpload mySmartUpload = new SmartUpload();
		try
		{
			mySmartUpload.initialize(pConfig, request, response);
			mySmartUpload.setAllowedFilesList("xls,xlsx,XLS,XLSX,");
			mySmartUpload.upload();

			this.Sid = mySmartUpload.getRequest().getParameter("Sid");
			CurrStatus currStatus = (CurrStatus) request.getSession().getAttribute("CurrStatus_" + this.Sid);
			currStatus.getHtmlData(request, pFromZone);
			String Project_Id = mySmartUpload.getRequest().getParameter("Project_Id");

			if ((mySmartUpload.getFiles().getCount() > 0) && (mySmartUpload.getFiles().getFile(0).getFilePathName().trim().length() > 0))
			{
				if (mySmartUpload.getFiles().getFile(0).getSize() / 1024 <= 3072)
				{
					com.jspsmart.upload.File myFile = mySmartUpload.getFiles().getFile(0);
					File_Name = mySmartUpload.getFiles().getFile(0).getFileName();
					myFile.saveAs(FileSaveRoute + Project_Id + "_" + File_Name);
				}
				else
				{
					currStatus.setResult("�ĵ��ϴ�ʧ�ܣ��ĵ����󣬱���С��3M!");
				}
			}
			currStatus.setJsp("loading.jsp?Sid=" + Sid + "&Project_Id=" + Project_Id + "&AnalogType=" + File_Name.substring(0, 2));
			request.getSession().setAttribute("CurrStatus_" + Sid, currStatus);
			response.sendRedirect(currStatus.getJsp());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * ��ˮ ����ܾ�ʱ��ˮλ - ˮλ�۾Q�D
	 * 
	 * @param gjId
	 * @return WaterAccGj
	 */
	public String AnalogWaterAccGj(String gjId)
	{
		AnalogWaterType = "WaterAccGj";
		return analog_Y4(null, 0, gjId, AnalogWaterType);
	}

	/**
	 * ��ˮ ����ʱ��ˮλ��� - ˮλ����D
	 * 
	 * @param subSys
	 * @param timePeriod
	 * @return WaterLev
	 */
	public String AnalogWaterLev(String subSys, int timePeriod)
	{
		AnalogWaterType = "WaterLev";
		return analog_Y4(subSys, timePeriod, null, AnalogWaterType);
	}

	/**
	 * ��ˮ ����ʱ�λ�ˮ�� - ģ�M�؈D�cλ�eˮ��
	 * 
	 * @param fileName
	 * @param timePeriod
	 * @return WaterAcc
	 */
	public String AnalogWaterAcc(String subSys)
	{
		AnalogWaterType = "WaterAcc";
		return analog_Y4(subSys, 0, null, AnalogWaterType);
	}

	/**
	 * ��ˮ ����ܾ�ʱ��ˮλ - ˮλ�۾Q�D
	 * 
	 * @param gjId
	 * @return WaterAccGj
	 */
	public String AnalogSewageAccGj(String gjId)
	{
		AnalogWaterType = "SewageAccGj";
		return analog_W1(null, 0, gjId, AnalogWaterType);
	}

	/**
	 * ��ˮ ����ʱ��ˮλ��� - ˮλ����D
	 * 
	 * @param subSys
	 * @param timePeriod
	 * @return WaterLev
	 */
	public String AnalogSewageLev(String subSys, int timePeriod)
	{
		AnalogWaterType = "SewageLev";
		return analog_W1(subSys, timePeriod, null, AnalogWaterType);
	}

	/**
	 * ��ˮ ����ʱ�λ�ˮ�� - ģ������ˮ��
	 * 
	 * @param fileName
	 * @param timePeriod
	 * @return WaterAcc
	 */
	public String AnalogSewageAcc(String subSys)
	{
		AnalogWaterType = "SewageAcc";
		return analog_W1(subSys, 0, null, AnalogWaterType);
	}

	// ��һ�װ汾
	private String analog_Y1(String subSys, int timePeriod, String gjId, String AnalogWaterType)
	{
		int SubgjId = 0;
		if (gjId != null)
		{
			SubgjId = CommUtil.StrToInt(gjId.substring(12, 15)) - 1;
		}
		try
		{
			// �����������ݣ�
			// �ܶ������ڵ������ܵ��������·�����ܶ����������������ģ��ʱ������֥�Ӹ���ʱ��λ��
			// �ܵ�·������·�����ڵ������յ�ڵ�ţ��м�������ļ�ָ��
			int NP = 9, NN = 10, Nstart = 3, Npline = 7, NT = 60, NR = 23, Nroute = 3, Nr_node = 8, Nend = 7, Iprt = 0;
			// ���깫ʽ����shanghai storm water formular:
			// (A1+C*lgP)/(t+b)**n---ln(N)=2.303log(N)---����ˮλ��m��
			// �ܶ����٣�m/s��, �ܶ��趨����vp0�����氼͹ϵ��csf
			double A1 = 17.53, C_storm = 0.95, tmin = 10, b_storm = 11.77, P_simu = 50, n_storm = 0.88, dt = 2.0, rc = 0.375, Hw_end = 4.1, vp0 = 0.8, csf = 3.0;

			// ��ϵͳ�ܶ����ݣ�
			int[] I0; // �ܶ����νڵ��I0,
			int[] J0; // ���νڵ��J0,
			double[] lp; // �ܶγ���
			double[] dpl; // �ܶ�ֱ��(m)
			double[] slp; // Ħ|��ϵ��
			double[] ZJup; // ���ιܵ׸߳�(m)
			double[] ZJdw; // ���ιܵ׸߳�(m)

			// ��ϵͳ�ڵ�����
			// ������ʼ�ڵ�ź���ʼ�ڵ�ܵ�����<m>
			double[] Aj; // �ڵ��ˮ���(ha)3.5
			double[] Acoef; // �ڵ��ˮ�������ϵ��0.6
			double[] Hj; // �ڵ�����ߣ�m��[NN=23]

			// ����·������·���ڵ��(-99��ʾ�սڵ�)
			int[][] Mroute;

			// ��ϵͳ��֧·���ܶ����ݾ��� ����pipe branches-reverse order
			int[][] Mbranch;

			
			String XlsPath = "";
			if (gjId != null)
			{
				XlsPath = FileSaveRoute + gjId.substring(0, 12) + ".xls";
				gjName = gjId.substring(0, 12);
			}
			else
			{
				XlsPath = FileSaveRoute + subSys + ".xls";
				gjName = subSys;
			}
			InputStream is = new FileInputStream(XlsPath);
			Workbook rwb = Workbook.getWorkbook(is);
			Sheet rs = rwb.getSheet(0);
			int rsRows = rs.getRows();

			/*
			 * �������ݱ����ϵͳ�� �ڵ���NN �ܶ���NP �����NStart ·���ܶ���Npline ·���ڵ���Nr_node
			 * �յ���ں�Nend ģ��ʱ��NT �ܶ�·����NrouteYJ002 10 9 3 7 8 8 60 3
			 */
			int rowCnt = 2;
			String sysName = rs.getCell(0, rowCnt).getContents().trim();
			NN = Integer.parseInt(rs.getCell(1, rowCnt).getContents().trim());
			NP = Integer.parseInt(rs.getCell(2, rowCnt).getContents().trim());
			Nstart = Integer.parseInt(rs.getCell(3, rowCnt).getContents().trim());
			Npline = Integer.parseInt(rs.getCell(4, rowCnt).getContents().trim());
			Nr_node = Integer.parseInt(rs.getCell(5, rowCnt).getContents().trim());
			Nend = Integer.parseInt(rs.getCell(6, rowCnt).getContents().trim());
			NT = Integer.parseInt(rs.getCell(7, rowCnt).getContents().trim());
			Nroute = Integer.parseInt(rs.getCell(8, rowCnt).getContents().trim());
			rowCnt += 4;

			/*
			 * ��ϵͳ�ܶ����ݱ�� Pipe.No ����I0 �յ��J0 ����LP ֱ��DP Ħ��ϵ�� ��˱�� �ն˱�� 1 0 1 28.5
			 * 0.3 0.017 3.894 3.842 2 1 2 32 0.3 0.017 3.842 3.784 3 2 3 28.6
			 * 0.3 0.017 3.784 3.733 4 3 4 25.4 0.3 0.017 3.733 3.687 5 4 5 24.7
			 * 0.3 0.017 3.687 3.643 6 5 6 23.5 0.3 0.017 3.643 3.601 7 6 7 30.4
			 * 0.3 0.017 3.601 3.546 8 8 7 15.5 0.3 0.017 3.731 3.171 9 9 6 4.3
			 * 0.3 0.017 3.886 3.7
			 */
			I0 = new int[NP];
			J0 = new int[NP];
			lp = new double[NP];
			dpl = new double[NP];
			slp = new double[NP];
			ZJup = new double[NP];
			ZJdw = new double[NP];
			for (int j = 0; j < NP; j++)
			{
				I0[j] = Integer.parseInt(rs.getCell(1, rowCnt + j).getContents().trim());
				J0[j] = Integer.parseInt(rs.getCell(2, rowCnt + j).getContents().trim());
				lp[j] = Double.parseDouble(rs.getCell(3, rowCnt + j).getContents().trim());
				dpl[j] = Double.parseDouble(rs.getCell(4, rowCnt + j).getContents().trim());
				slp[j] = Double.parseDouble(rs.getCell(5, rowCnt + j).getContents().trim());
				ZJup[j] = Double.parseDouble(rs.getCell(6, rowCnt + j).getContents().trim());
				ZJdw[j] = Double.parseDouble(rs.getCell(7, rowCnt + j).getContents().trim());
			}
			rowCnt += NP;
			rowCnt += 3;

			/*
			 * ��ϵͳ�ڵ����ݱ��ڵ�No ��ˮ���ha ����ϵ�� ������ ���ױ�� 1 3.5 0.6 5.244 ��δ�õ� 2 3.5
			 * 0.6 5.191 3 3.5 0.6 5.177 4 3.5 0.6 5.208 5 3.5 0.6 5.221 6 3.5
			 * 0.6 5.201 7 3.5 0.6 5.2 8 3.5 0.6 5.121 9 3.5 0.6 5.131 10 3.5
			 * 0.6 5.186
			 */
			Aj = new double[NN];
			Acoef = new double[NN];
			Hj = new double[NN];
			for (int j = 0; j < NN; j++)
			{
				Aj[j] = Double.parseDouble(rs.getCell(1, rowCnt + j).getContents().trim());
				Acoef[j] = Double.parseDouble(rs.getCell(2, rowCnt + j).getContents().trim());
				Hj[j] = Double.parseDouble(rs.getCell(3, rowCnt + j).getContents().trim());
			}
			rowCnt += NN;
			rowCnt += 3;

			/*
			 * ����·����&·���ڵ�Žڵ���� 1 2 3 4 5 6 7 8 1 0 1 2 3 4 5 6 7 2 8 7 -99 -99
			 * -99 -99 -99 -99 3 9 6 -99 -99 -99 -99 -99 -99
			 */
			Mroute = new int[Nstart][Nr_node];
			for (int j = 0; j < Nstart; j++)
			{
				for (int k = 0; k < Nr_node; k++)
				{
					Mroute[j][k] = Integer.parseInt(rs.getCell(k + 1, rowCnt + j).getContents().trim());
				}
			}
			rowCnt += Nstart;
			rowCnt += 3;

			/*
			 * ��ϵͳ��֧·���ܶ����ݾ��� ����pipe branches-reverse order �ڵ���� 1 2 3 4 5 6 7 1
			 * 6 5 4 3 2 1 0 2 7 -99 -99 -99 -99 -99 -99 3 8 -99 -99 -99 -99 -99
			 * -99
			 */
			Mbranch = new int[Nstart][Npline];
			for (int j = 0; j < Nstart; j++)
			{
				for (int k = 0; k < Npline; k++)
				{
					Mbranch[j][k] = Integer.parseInt(rs.getCell(k + 1, rowCnt + j).getContents().trim());
				}
			}
			// ----�ٽ�ˮ��������----
			double sita0 = 3.0, eps = 0.001, alfa = 0.5;
			double Ad0, qkpmax, Hwdwkp, yykp, sita, cons_b, sita_s = 0, sita_c, fsita, dfdsita, dfsita, ssita = 0, csita = 0, hyd_A, hafsita, shafsita = 0, chafsita, sita_p = 0;
			// �м����
			int i, j, k = 0, ik, jk, it, k1, kp, in1, in2, in3;
			double dtnt, taa, tbb, AA, XX1, XX2, hdj0;
			double[] XX = new double[NT];
			double[] qit = new double[NT];
			double[][] sumqj = new double[NT][NN];
			double[][] sumAj = new double[NT][NN];
			double[][] Tnode = new double[NN][NN];
			double[][] sumTnode = new double[NN][NN];
			double[] vp = new double[NP];
			double[] slop = new double[NP];
			double[][] qpt = new double[NT][NP];
			double[][] qqkp = new double[NT][NP];
			double[][] vpt = new double[NT][NP];
			double[][] rid = new double[NT][NP];
			double[][] slopt = new double[NT][NP];
			double[][] Hwup = new double[NT][NP];
			double[][] Hwdw = new double[NT][NP];
			double[][] hdcc0 = new double[NT][NP];
			double[][] overflow = new double[NT][NN];
			double[][] Hw_over = new double[NT][NN];
			double[][] Hwj = new double[NT][NN];

			DecimalFormat df = new DecimalFormat("##.####");
			DecimalFormat df1 = new DecimalFormat("######.##");
			// ================= ����ֵ ===============================
			for (i = 0; i < NT; i++)
			{
				for (j = 0; j < NN; j++)
					sumAj[i][j] = 0;
			}
			for (i = 0; i < NT; i++)
			{
				for (j = 0; j < NN; j++)
					sumqj[i][j] = 0;
			}
			for (i = 0; i < NN; i++)
			{
				for (j = 0; j < NN; j++)
				{
					if (i == j)
					{
						Tnode[i][j] = 0;
					}
					else
					{
						Tnode[i][j] = -99;
					}
				}
			}
			for (i = 0; i < NN; i++)
			{
				for (j = 0; j < NN; j++)
					sumTnode[i][j] = 0;
			}
			for (i = 0; i < NP; i++)
				vp[i] = vp0;
			for (kp = 0; kp < NP; kp++)
			{
				in1 = I0[kp];
				in2 = J0[kp];
				Tnode[in1][in2] = lp[kp] / vp[kp] / 60;
				slop[kp] = (ZJup[kp] - ZJdw[kp]) / lp[kp];
			}
			//
			for (i = 0; i < Nroute; i++)
			{
				for (j = 0; j < Nr_node; j++)
				{
					in1 = Mroute[i][j];
					if (in1 >= 0)
					{
						for (k = j + 1; k < Nr_node; k++)
						{
							in2 = Mroute[i][k - 1];
							in3 = Mroute[i][k];
							if (in3 >= 0)
							{
								sumTnode[in1][in3] = sumTnode[in1][in2] + Tnode[in2][in3];
							}
						}
					}
				}
			}
			// ----------------�ڵ��ˮ���(ha)�ͻ�ˮ����(m3/sec)����--------//
			// ֥�Ӹ������--rainfall intensity at every time step--
			AA = A1 + A1 * C_storm * Math.log(P_simu) / 2.303;
			for (it = 0; it < NT; it++)
			{
				if (it <= NR)
				{
					dtnt = dt * (float) (it);
					tbb = dt * (float) (NR) - dtnt;
					XX1 = AA * ((1.0 - n_storm) * tbb / rc + b_storm);
					XX2 = Math.pow((tbb / rc + b_storm), (n_storm + 1.0));
				}
				else
				{
					dtnt = dt * (float) (it);
					taa = dtnt - dt * (float) (NR);
					XX1 = AA * ((1.0 - n_storm) * taa / (1.0 - rc) + b_storm);
					XX2 = Math.pow((taa / (1.0 - rc) + b_storm), (n_storm + 1.0));
				}
				XX[it] = XX1 / XX2;
				qit[it] = 167.0 * XX[it] / 1000.0;
			}
			for (it = 0; it < NT; it++)
			{
				dtnt = dt * (float) (it);
			}
			for (it = 0; it < NT; it++)
			{
				dtnt = dt + dt * (float) (it);
				for (j = 0; j < NN; j++)
				{
					sumAj[it][j] = Aj[j];
					sumqj[it][j] = Aj[j] * qit[it] * Acoef[j];
					for (i = 0; i < NN; i++)
					{
						if (sumTnode[i][j] > 0 && sumTnode[i][j] < dtnt)
						{
							sumAj[it][j] = sumAj[it][j] + Aj[i];
							sumqj[it][j] = sumqj[it][j] + Aj[i] * qit[it] * Acoef[i];
						}
					}
				}
			}
			for (it = 0; it < NT; it++)
			{
				for (i = 0; i < NN; i++)
				{
					overflow[it][i] = 0.0;
					Hw_over[it][i] = 0.0;
				}
			}
			for (it = 0; it < NT; it++)
			{
				for (j = 0; j < NP; j++)
				{
					qpt[it][j] = -99.0;
					qqkp[it][j] = 0.0;
				}
			}
			for (it = 0; it < NT; it++)
			{
				for (j = 0; j < NN; j++)
				{
					for (k = 0; k < NP; k++)
					{
						if (I0[k] == j)
						{
							qpt[it][k] = sumqj[it][j];
						}
					}
				}
				for (ik = 0; ik < Nstart; ik++)
				{
					for (jk = 0; jk < Npline; jk++)
					{
						kp = Mbranch[ik][jk];
						if (kp >= 0)
						{
							if (J0[kp] == Nend)
							{
								Hwdw[it][kp] = Hw_end;
							}
							else
							{
								for (k1 = 0; k1 < NP; k1++)
								{
									if (I0[k1] == J0[kp]) Hwdw[it][kp] = Hwup[it][k1];
								}
							}
							Ad0 = 0.7854 * Math.pow(dpl[kp], 2.0);
							hdj0 = ZJdw[kp] + dpl[kp];
							if (Hwdw[it][kp] >= hdj0)
							{
								hdcc0[it][kp] = 1.0;
								rid[it][kp] = dpl[kp] / 4.0;
								vpt[it][kp] = qpt[it][kp] / Ad0;
								slopt[it][kp] = 10.29 * Math.pow(slp[kp], 2.0) * Math.pow(qpt[it][kp], 2.0) / Math.pow(dpl[kp], 5.333);
								Hwup[it][kp] = Hwdw[it][kp] + slopt[it][kp] * lp[kp];
								if (Hwup[it][kp] >= Hj[I0[kp]])
								{
									Hwup[it][kp] = Hj[I0[kp]];
									slopt[it][kp] = (Hwup[it][kp] - Hwdw[it][kp]) / lp[kp];
									if (slopt[it][kp] < 0.0)
									{
										slopt[it][kp] = Math.abs(slopt[it][kp]);
									}
									vpt[it][kp] = Math.pow(rid[it][kp], 0.6667) * Math.pow(slopt[it][kp], 0.5) / slp[kp];
									qqkp[it][kp] = vpt[it][kp] * Ad0;
									if (qqkp[it][kp] < 0.0)
									{
										qqkp[it][kp] = Math.abs(qqkp[it][kp]);
									}
								}
							}
							else
							{
								qkpmax = 2.46 * Math.pow(dpl[kp], 2.5);
								if (qpt[it][kp] > qkpmax * 0.95)
								{
									Hwdw[it][kp] = ZJdw[kp] + dpl[kp] * 1.1;
									hdcc0[it][kp] = 1.0;
									rid[it][kp] = dpl[kp] / 4.0;
									vpt[it][kp] = qpt[it][kp] / Ad0;
									slopt[it][kp] = 10.29 * Math.pow(slp[kp], 2.0) * Math.pow(qpt[it][kp], 2.0) / Math.pow(dpl[kp], 5.333);
									Hwup[it][kp] = Hwdw[it][kp] + slopt[it][kp] * lp[kp];
									if (Hwup[it][kp] >= Hj[I0[kp]])
									{
										Hwup[it][kp] = Hj[I0[kp]];
										slopt[it][kp] = (Hwup[it][kp] - Hwdw[it][kp]) / lp[kp];
										if (slopt[it][kp] < 0.0)
										{
											slopt[it][kp] = Math.abs(slopt[it][kp]);
										}
										vpt[it][kp] = Math.pow(rid[it][kp], 0.6667) * Math.pow(slopt[it][kp], 0.5) / slp[kp];
										qqkp[it][kp] = vpt[it][kp] * Ad0;
										if (qqkp[it][kp] < 0.0)
										{
											qqkp[it][kp] = Math.abs(qqkp[it][kp]);
										}
									}
								}
								else
								{
									i = 0;
									sita = sita0;
									cons_b = 0.276843 * Math.pow(dpl[kp], 2.5) / qpt[it][kp];
									while (true)
									{
										ssita = Math.sin(sita);
										csita = Math.cos(sita);
										hafsita = sita / 2.0;
										shafsita = Math.sin(hafsita);
										chafsita = Math.cos(hafsita);
										sita_s = sita - Math.sin(sita);
										sita_c = 1 - Math.cos(sita);
										sita_p = Math.pow((1.0 - chafsita), -0.5);
										fsita = cons_b * sita_s - sita_p;
										dfsita = Math.abs(fsita);
										if (dfsita < eps)
										{
											hdcc0[it][kp] = (1 - Math.cos(sita / 2)) / 2;
											rid[it][kp] = 0.25 * dpl[kp] * (sita - Math.sin(sita)) / sita;
											vpt[it][kp] = Math.pow(rid[it][kp], 0.6667) * Math.pow(slop[kp], 0.5) / slp[kp];
											break;
										}
										else
										{
											dfdsita = cons_b * (1.0 - csita) + 0.25 * Math.pow(sita_p, -1.0) * shafsita;
											sita = sita - alfa * fsita / dfdsita;
											i = i + 1;
										}
									}
								}
								Hwdwkp = ZJdw[kp] + hdcc0[it][kp] * dpl[kp];
								if (Hwdwkp >= Hwdw[it][kp])
								{
									Hwdw[it][kp] = Hwdwkp;
								}
								if (Hwdwkp < Hwdw[it][kp])
								{
									yykp = Hwdw[it][kp] - ZJdw[kp];
									if (yykp > dpl[kp])
									{
										yykp = dpl[kp];
									}
									sita = 2.0 * Math.acos(1.0 - 2.0 * yykp / dpl[kp]);
									hdcc0[it][kp] = yykp / dpl[kp];
									rid[it][kp] = 0.25 * dpl[kp] * (sita - Math.sin(sita)) / sita;
									vpt[it][kp] = Math.pow(rid[it][kp], 0.6667) * Math.pow(slop[kp], 0.5) / slp[kp];
								}
								Hwup[it][kp] = Hwdw[it][kp] + slop[kp] * lp[kp];
							}
						}
					}
				}
				for (i = 0; i < NP; i++)
				{
					k = J0[i];
					if (k == Nend)
					{
						Hwj[it][k] = Hwdw[it][i];
					}
					{
						j = I0[i];
						Hwj[it][j] = Hwup[it][i];
						if (Hwup[it][i] == Hj[j])
						{
							overflow[it][j] = overflow[it][j] + (qpt[it][i] - qqkp[it][i]) * dt * 60.0;
							Hw_over[it][j] = csf * overflow[it][j] / Aj[j] / 10000.0 * 1000.0;

						}
						if (Hwup[it][i] < Hj[j] && overflow[it][j] > 0.0)
						{
							overflow[it][j] = overflow[it - 1][j] * 0.90;
							Hw_over[it][j] = csf * overflow[it][j] / Aj[j] / 10000.0 * 1000.0;
						}
					}
					if (it > NR && Hw_over[it][j] <= 5.0)
					{
						overflow[it][j] = 0.0;
						Hw_over[it][j] = 0.0;
					}
				}
			}
			// ʱ�ιܾ�ˮλ����ͼ�͹ܾ�ˮλʱ������ͼ�����֯
			for (it = 0; it < NT; it++)
			{
				String WaterLevNew = "";
				for (i = 0; i < NN; i++)
				{
					if (gjId != null && i == SubgjId)
					{
						WaterAccGj += df1.format(Hwj[it][i]) + "|";
					}
					WaterLevNew += df1.format(Hwj[it][i]) + "|";
				}
				WaterLev[it] = WaterLevNew;
			}
			// �����ˮ�������֯
			for (it = 0; it < NT; it++)
			{
				String WaterAccNew = "";
				for (i = 0; i < NN; i++)
				{
					if (overflow[it][i] <= 0.0)
					{
						WaterAccNew += 0 + "|";
					}
					else
					{
						WaterAccNew += df1.format(overflow[it][i]) + "|";
					}
				}
				WaterAcc[it] = WaterAccNew;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return gjName;
		}
		if (AnalogWaterType.equals("WaterAccGj"))
		{
			return WaterAccGj;
		}
		else if (AnalogWaterType.equals("WaterAcc"))
		{
			String WaterAccList = "";
			for (int i = 0; i < WaterAcc.length; i++)
			{
				WaterAccList += subSys.substring(7, 12) + WaterAcc[i] + ";";
			}
			return WaterAccList;
		}
		else if (AnalogWaterType.equals("WaterLev"))
		{
			String WaterLevList = "";
			for (int i = 0; i < WaterLev.length; i++)
			{
				WaterLevList += subSys.substring(7, 12) + WaterLev[i] + ";";
			}
			return WaterLevList;
			// return WaterLev[timePeriod];
		}
		return "";
	}

	// �ڶ��װ汾
	private String analog_Y2(String subSys, int timePeriod, String gjId, String AnalogWaterType)
	{
		int SubgjId = 0;
		if (gjId != null)
		{
			SubgjId = CommUtil.StrToInt(gjId.substring(12, 15)) - 1;
		}
		try
		{
			// �����������ݣ�
			// �ܶ������ڵ������ܵ��������·�����ܶ����������������ģ��ʱ������֥�Ӹ���ʱ��λ��
			// �ܵ�·������·�����ڵ������յ�ڵ�ţ��м�������ļ�ָ��
			int NP = 9, NN = 10, Nstart = 3, Npline = 7, NT = 60, NR = 23, Nroute = 3, Nr_node = 8, Nend = 7, Iprt = 0, Nprtc = 20;
			// ���깫ʽ����shanghai storm water formular:
			// (A1+C*lgP)/(t+b)**n---ln(N)=2.303log(N)---����ˮλ��m��
			// �ܶ����٣�m/s��, �ܶ��趨����vp0�����氼͹ϵ��csf
			double A1 = 17.53, C_storm = 0.95, tmin = 10, b_storm = 11.77, P_simu = 100, n_storm = 0.88, dt = 2.0, rc = 0.375, Hw_end = 4.1, vp0 = 0.8, csf = 3.0;

			// ��ϵͳ�ܶ����ݣ�
			int[] I0; // �ܶ����νڵ��I0,
			int[] J0; // ���νڵ��J0,
			double[] lp; // �ܶγ���
			double[] dpl; // �ܶ�ֱ��(m)
			double[] slp; // Ħ|��ϵ��
			double[] ZJup; // ���ιܵ׸߳�(m)
			double[] ZJdw; // ���ιܵ׸߳�(m)

			// ��ϵͳ�ڵ�����
			// ������ʼ�ڵ�ź���ʼ�ڵ�ܵ�����<m>
			double[] Aj; // �ڵ��ˮ���(ha)3.5
			double[] Acoef; // �ڵ��ˮ�������ϵ��0.6
			double[] Hj; // �ڵ�����ߣ�m��[NN=23]

			// ����·������·���ڵ��(-99��ʾ�սڵ�)
			int[][] Mroute;

			// ��ϵͳ��֧·���ܶ����ݾ��� ����pipe branches-reverse order
			int[][] Mbranch;

			String XlsPath = "";
			if (gjId != null)
			{
				XlsPath = FileSaveRoute + gjId.substring(0, 12) + ".xls";
				gjName = gjId.substring(0, 12);
			}
			else
			{
				XlsPath = FileSaveRoute + subSys + ".xls";
				gjName = subSys;
			}
			InputStream is = new FileInputStream(XlsPath);
			Workbook rwb = Workbook.getWorkbook(is);
			Sheet rs = rwb.getSheet(0);
			int rsRows = rs.getRows();

			/*
			 * �������ݱ����ϵͳ�� �ڵ���NN �ܶ���NP �����NStart ·���ܶ���Npline ·���ڵ���Nr_node
			 * �յ���ں�Nend ģ��ʱ��NT �ܶ�·����NrouteYJ002 10 9 3 7 8 8 60 3
			 */
			int rowCnt = 2;
			String sysName = rs.getCell(0, rowCnt).getContents().trim();
			NN = Integer.parseInt(rs.getCell(1, rowCnt).getContents().trim());
			NP = Integer.parseInt(rs.getCell(2, rowCnt).getContents().trim());
			Nstart = Integer.parseInt(rs.getCell(3, rowCnt).getContents().trim());
			Npline = Integer.parseInt(rs.getCell(4, rowCnt).getContents().trim());
			Nr_node = Integer.parseInt(rs.getCell(5, rowCnt).getContents().trim());
			Nend = Integer.parseInt(rs.getCell(6, rowCnt).getContents().trim());
			NT = Integer.parseInt(rs.getCell(7, rowCnt).getContents().trim());
			Nroute = Integer.parseInt(rs.getCell(8, rowCnt).getContents().trim());
			rowCnt += 4;

			/*
			 * ��ϵͳ�ܶ����ݱ�� Pipe.No ����I0 �յ��J0 ����LP ֱ��DP Ħ��ϵ�� ��˱�� �ն˱�� 1 0 1 28.5
			 * 0.3 0.017 3.894 3.842 2 1 2 32 0.3 0.017 3.842 3.784 3 2 3 28.6
			 * 0.3 0.017 3.784 3.733 4 3 4 25.4 0.3 0.017 3.733 3.687 5 4 5 24.7
			 * 0.3 0.017 3.687 3.643 6 5 6 23.5 0.3 0.017 3.643 3.601 7 6 7 30.4
			 * 0.3 0.017 3.601 3.546 8 8 7 15.5 0.3 0.017 3.731 3.171 9 9 6 4.3
			 * 0.3 0.017 3.886 3.7
			 */
			I0 = new int[NP];
			J0 = new int[NP];
			lp = new double[NP];
			dpl = new double[NP];
			slp = new double[NP];
			ZJup = new double[NP];
			ZJdw = new double[NP];
			for (int j = 0; j < NP; j++)
			{
				I0[j] = Integer.parseInt(rs.getCell(1, rowCnt + j).getContents().trim());
				J0[j] = Integer.parseInt(rs.getCell(2, rowCnt + j).getContents().trim());
				lp[j] = Double.parseDouble(rs.getCell(3, rowCnt + j).getContents().trim());
				dpl[j] = Double.parseDouble(rs.getCell(4, rowCnt + j).getContents().trim());
				slp[j] = Double.parseDouble(rs.getCell(5, rowCnt + j).getContents().trim());
				ZJup[j] = Double.parseDouble(rs.getCell(6, rowCnt + j).getContents().trim());
				ZJdw[j] = Double.parseDouble(rs.getCell(7, rowCnt + j).getContents().trim());
			}
			rowCnt += NP;
			rowCnt += 3;

			/*
			 * ��ϵͳ�ڵ����ݱ��ڵ�No ��ˮ���ha ����ϵ�� ������ ���ױ�� 1 3.5 0.6 5.244 ��δ�õ� 2 3.5
			 * 0.6 5.191 3 3.5 0.6 5.177 4 3.5 0.6 5.208 5 3.5 0.6 5.221 6 3.5
			 * 0.6 5.201 7 3.5 0.6 5.2 8 3.5 0.6 5.121 9 3.5 0.6 5.131 10 3.5
			 * 0.6 5.186
			 */
			Aj = new double[NN];
			Acoef = new double[NN];
			Hj = new double[NN];
			for (int j = 0; j < NN; j++)
			{
				Aj[j] = Double.parseDouble(rs.getCell(1, rowCnt + j).getContents().trim());
				Acoef[j] = Double.parseDouble(rs.getCell(2, rowCnt + j).getContents().trim());
				Hj[j] = Double.parseDouble(rs.getCell(3, rowCnt + j).getContents().trim());
			}
			rowCnt += NN;
			rowCnt += 3;

			/*
			 * ����·����&·���ڵ�Žڵ���� 1 2 3 4 5 6 7 8 1 0 1 2 3 4 5 6 7 2 8 7 -99 -99
			 * -99 -99 -99 -99 3 9 6 -99 -99 -99 -99 -99 -99
			 */
			Mroute = new int[Nstart][Nr_node];
			for (int j = 0; j < Nstart; j++)
			{
				for (int k = 0; k < Nr_node; k++)
				{
					Mroute[j][k] = Integer.parseInt(rs.getCell(k + 1, rowCnt + j).getContents().trim());
				}
			}
			rowCnt += Nstart;
			rowCnt += 3;

			/*
			 * ��ϵͳ��֧·���ܶ����ݾ��� ����pipe branches-reverse order �ڵ���� 1 2 3 4 5 6 7 1
			 * 6 5 4 3 2 1 0 2 7 -99 -99 -99 -99 -99 -99 3 8 -99 -99 -99 -99 -99
			 * -99
			 */
			Mbranch = new int[Nstart][Npline];
			for (int j = 0; j < Nstart; j++)
			{
				for (int k = 0; k < Npline; k++)
				{
					Mbranch[j][k] = Integer.parseInt(rs.getCell(k + 1, rowCnt + j).getContents().trim());
				}
			}
			// ----�ٽ�ˮ��������----
			double sita0 = 3.0, eps = 0.001, alfa = 0.5;
			double Ad0, qkpmax, Hwdwkp, yykp, sita, cons_b, sita_s = 0, sita_c, fsita, dfdsita, dfsita, ssita = 0, csita = 0, hyd_A, hafsita, shafsita = 0, chafsita, sita_p = 0;
			// �м����
			int i, j, k, ik, jk, it, k1, kp, in1, in2, in3, NR1, NR2, ii, Nprt, iprt1, iprt2;
			double H00, ycd0;
			double dtnt, taa, tbb, AA, XX1, XX2, hdj0;
			double[] XX = new double[NT];
			double[] qit = new double[NT];
			double[][] sumqj = new double[NT][NN];
			double[][] sumAj = new double[NT][NN];
			double[][] Tnode = new double[NN][NN];
			double[][] sumTnode = new double[NN][NN];
			double[] vp = new double[NP];
			double[] slop = new double[NP];
			double[][] qpt = new double[NT][NP];
			double[][] qqkp = new double[NT][NP];
			double[][] vpt = new double[NT][NP];
			double[][] rid = new double[NT][NP];
			double[][] slopt = new double[NT][NP];
			double[][] Hwup = new double[NT][NP];
			double[][] Hwdw = new double[NT][NP];
			double[][] hdcc0 = new double[NT][NP];
			double[][] overflow = new double[NT][NN];
			double[][] Hw_over = new double[NT][NN];
			double[][] Hwj = new double[NT][NN];

			// ----------------------------------------------------------------------------------------------------------
			String FileName = "";
			if (gjId != null)
			{
				FileName = gjId.substring(0, 12) + ".txt";
			}
			else
			{
				FileName = subSys + ".txt";
			}
			FileOutputStream fs = new FileOutputStream(new File(FilePath + FileName));
			PrintStream printStream = new PrintStream(fs);
			printStream.println(FileName);

			DecimalFormat df = new DecimalFormat("##.####");
			DecimalFormat df1 = new DecimalFormat("######.##");
			// --��������ļ���ʼ---
			// ================= ����ֵ ===============================
			for (i = 0; i < NT; i++)
			{
				for (j = 0; j < NN; j++)
					sumAj[i][j] = 0;
			}
			for (i = 0; i < NT; i++)
			{
				for (j = 0; j < NN; j++)
					sumqj[i][j] = 0;
			}
			for (i = 0; i < NN; i++)
			{
				for (j = 0; j < NN; j++)
				{
					if (i == j)
					{
						Tnode[i][j] = 0;
					}
					else
					{
						Tnode[i][j] = -99;
					}
				}
			}
			for (i = 0; i < NN; i++)
			{
				for (j = 0; j < NN; j++)
					sumTnode[i][j] = 0;
			}
			// ==================Tnode-sumTnode=========================
			for (i = 0; i < NP; i++)
				vp[i] = vp0;
			for (kp = 0; kp < NP; kp++)
			{
				in1 = I0[kp];
				in2 = J0[kp];
				Tnode[in1][in2] = lp[kp] / vp[kp] / 60;
				slop[kp] = (ZJup[kp] - ZJdw[kp]) / lp[kp];
			}
			for (i = 0; i < Nroute; i++)
			{
				for (j = 0; j < Nr_node; j++)
				{
					in1 = Mroute[i][j];
					if (in1 >= 0)
					{
						for (k = j + 1; k < Nr_node; k++)
						{
							in2 = Mroute[i][k - 1];
							in3 = Mroute[i][k];
							if (in3 >= 0)
							{
								sumTnode[in1][in3] = sumTnode[in1][in2] + Tnode[in2][in3];
							}
						}
					}
				}
			}
			// System.out.println("pipe no.  I0    J0");
			for (i = 0; i < NP; i++)
			{
				// System.out.printf("%6d%6d%6d", i, I0[i], J0[i]);
				// System.out.println();
			}
			printStream.println();
			printStream.print(" ip=");
			for (i = 0; i < NP; i++)
			{
				printStream.printf("%4d", i);
			}
			printStream.println();
			printStream.print(" I0=");
			for (i = 0; i < NP; i++)
			{
				printStream.printf("%4d", I0[i]);
			}
			printStream.println();
			printStream.print(" J0=");
			for (i = 0; i < NP; i++)
			{
				printStream.printf("%4d", J0[i]);
			}
			printStream.println();
			printStream.println();
			printStream.println("===========  print Mroute[i][j]");
			for (i = 0; i < Nroute; i++)
			{
				for (j = 0; j < Nr_node; j++)
				{
					printStream.printf("%6d", Mroute[i][j]);
				}
				printStream.println();
			}
			printStream.println();
			printStream.println("===========  print Mbranch[i][j]");
			for (i = 0; i < Nstart; i++)
			{
				for (j = 0; j < Npline; j++)
				{
					printStream.printf("%6d", Mbranch[i][j]);
				}
				printStream.println();
			}
			printStream.println("===========  print Tnode[i][j]");
			printStream.println("====j=  ");
			printStream.println("      ");
			for (j = 0; j < NN; j++)
			{
				printStream.printf("%6d", j);
			}
			printStream.println();
			for (i = 0; i < NN; i++)
			{
				if (i < 10)
				{
					printStream.print("i=" + i + "   ");
				}
				else
				{
					printStream.print("i=" + i + "  ");
				}
				for (j = 0; j < NN; j++)
				{
					if (Tnode[i][j] < 0.0)
					{
						printStream.print("      ");
					}
					else
					{
						printStream.printf("%6.2f", Tnode[i][j]);
					}
				}
				printStream.println();
			}
			printStream.println();
			printStream.println("===========  print sumTnode[i][j]");
			printStream.print("==j=  ");
			for (j = 0; j < NN; j++)
			{
				printStream.printf("%6d", j);
			}
			printStream.println();
			for (i = 0; i < NN; i++)
			{
				printStream.print("i=" + i + "   ");
				for (j = 0; j < NN; j++)
				{
					if (sumTnode[i][j] <= 0.0)
					{
						printStream.print("      ");
					}
					else
					{
						printStream.printf("%6.2f", sumTnode[i][j]);
					}
				}
				printStream.println();
			}
			// ================= ����׼��̬����ģ��============================
			// -------------------��̬ģ����������-----------------------------
			// ----------------�ڵ��ˮ���(ha)�ͻ�ˮ����(m3/sec)����--------
			printStream.println();
			printStream.println("===========  ������̬ģ�����      �����ڣ� " + P_simu + "  ��   ʱ������ " + NT + "       �յ�ˮλ�� " + Hw_end + "  m  =========");
			// ֥�Ӹ������--rainfall intensity at every time step--
			AA = A1 + A1 * C_storm * Math.log(P_simu) / 2.303;
			for (it = 0; it < NT; it++)
			{
				if (it <= NR)
				{
					dtnt = dt * (float) (it);
					tbb = dt * (float) (NR) - dtnt;
					XX1 = AA * ((1.0 - n_storm) * tbb / rc + b_storm);
					XX2 = Math.pow((tbb / rc + b_storm), (n_storm + 1.0));
				}
				else
				{
					dtnt = dt * (float) (it);
					taa = dtnt - dt * (float) (NR);
					XX1 = AA * ((1.0 - n_storm) * taa / (1.0 - rc) + b_storm);
					XX2 = Math.pow((taa / (1.0 - rc) + b_storm), (n_storm + 1.0));
				}
				XX[it] = XX1 / XX2;
				qit[it] = 167.0 * XX[it] / 1000.0;
			}
			NR1 = NR - 1;
			NR2 = NR + 1;
			qit[NR] = (qit[NR] + qit[NR - 1] + qit[NR + 1]) / 3.0;
			printStream.println();
			printStream.println("    it      dtnt      XX[it]     qit[it]");
			for (it = 0; it < NT; it++)
			{
				dtnt = dt * (float) (it);
				printStream.printf("%6d%10.2f%12.6f%12.6f", it, dtnt, XX[it], qit[it]);
				printStream.println();
			}
			printStream.println();
			for (it = 0; it < NT; it++)
			{
				dtnt = dt + dt * (float) (it);
				for (j = 0; j < NN; j++)
				{
					sumAj[it][j] = Aj[j];
					sumqj[it][j] = Aj[j] * qit[it] * Acoef[j];
					for (i = 0; i < NN; i++)
					{
						if (sumTnode[i][j] > 0 && sumTnode[i][j] < dtnt)
						{
							sumAj[it][j] = sumAj[it][j] + Aj[i];
							sumqj[it][j] = sumqj[it][j] + Aj[i] * qit[it] * Acoef[i];
						}
					}
				}
			}
			printStream.println("  sumAj[it][j]=");
			for (it = 0; it < NT; it++)
			{
				for (j = 0; j < NN; j++)
				{
					printStream.printf("%8.2f", sumAj[it][j]);
				}
				printStream.println();
			}
			printStream.println();
			printStream.println("  sumqj[it][j]=");
			for (it = 0; it < NT; it++)
			{
				for (j = 0; j < NN; j++)
				{
					printStream.printf("%8.2f", sumqj[it][j]);
				}
				printStream.println();
			}
			printStream.println();
			// ---------------------------------------------------------------
			for (it = 0; it < NT; it++)
			{
				for (i = 0; i < NN; i++)
				{
					overflow[it][i] = 0.0;
					Hw_over[it][i] = 0.0;
				}
			}
			for (it = 0; it < NT; it++)
			{
				for (j = 0; j < NP; j++)
				{
					qpt[it][j] = -99.0;
					qqkp[it][j] = 0.0;
				}
			}
			// ---------------------------------------------------------------
			for (it = 0; it < NT; it++)
			{
				printStream.print(" it=" + it + "  qpt[it][k]=");
				for (j = 0; j < NN; j++)
				{
					for (k = 0; k < NP; k++)
					{
						if (I0[k] == j)
						{
							qpt[it][k] = sumqj[it][j];
							printStream.printf("%8.2f", qpt[it][k]);
						}
					}
				}
				printStream.println();
				for (ik = 0; ik < Nstart; ik++)
				{
					for (jk = 0; jk < Npline; jk++)
					{
						kp = Mbranch[ik][jk];
						if (kp >= 0)
						{
							if (J0[kp] == Nend)
							{
								Hwdw[it][kp] = Hw_end;
								if (1 == Iprt)
								{
									printStream.println("   it= " + it + "   kp= " + kp + "  Hwdm= " + Hwdw[it][kp] + "  Hw_end= " + Hw_end);
								}
							}
							else
							{
								for (k1 = 0; k1 < NP; k1++)
								{
									if (I0[k1] == J0[kp]) Hwdw[it][kp] = Hwup[it][k1];
								}
							}
							Ad0 = 0.7854 * Math.pow(dpl[kp], 2.0);
							hdj0 = ZJdw[kp] + dpl[kp];
							if (Hwdw[it][kp] >= hdj0)
							{
								if (1 == Iprt)
								{
									printStream.println("   it= " + it + "   kp= " + kp + "  Hwdm= " + df.format(Hwdw[it][kp]) + "  ��û���� ");
								}
								hdcc0[it][kp] = 1.0;
								rid[it][kp] = dpl[kp] / 4.0;
								vpt[it][kp] = qpt[it][kp] / Ad0;
								slopt[it][kp] = 10.29 * Math.pow(slp[kp], 2.0) * Math.pow(qpt[it][kp], 2.0) / Math.pow(dpl[kp], 5.333);
								Hwup[it][kp] = Hwdw[it][kp] + slopt[it][kp] * lp[kp];
								if (Hwup[it][kp] >= Hj[I0[kp]])
								{
									Hwup[it][kp] = Hj[I0[kp]];
									slopt[it][kp] = (Hwup[it][kp] - Hwdw[it][kp]) / lp[kp];
									if (slopt[it][kp] < 0.0)
									{
										slopt[it][kp] = Math.abs(slopt[it][kp]);
									}
									vpt[it][kp] = Math.pow(rid[it][kp], 0.6667) * Math.pow(slopt[it][kp], 0.5) / slp[kp];
									qqkp[it][kp] = vpt[it][kp] * Ad0;
									if (qqkp[it][kp] < 0.0)
									{
										qqkp[it][kp] = Math.abs(qqkp[it][kp]);
									}
								}
							}
							else
							{
								if (Iprt == 1)
								{
									printStream.println("   it= " + it + "   kp= " + kp + "  Hwdw= " + Hwdw[it][kp] + "  ����û���� ");
								}
								// --20161018�޸Ŀ�ʼ---�����ٽ�ˮ����㷨-----------------------
								qkpmax = 2.699 * Math.pow(dpl[kp], 2.5);
								if (qpt[it][kp] > qkpmax)
								{
									if (Iprt == 1)
									{
										printStream.println("   it= " + it + "   kp= " + kp + "  qkpmax= " + qkpmax + "  ����û���ܳ��� ");
									}
									vpt[it][kp] = qpt[it][kp] / Ad0;
									H00 = Math.pow(vpt[it][kp], 2.0) / 13.72;
									Hwdw[it][kp] = ZJdw[kp] + dpl[kp] + H00;
									hdcc0[it][kp] = 1.0;
									rid[it][kp] = dpl[kp] / 4.0;
									slopt[it][kp] = 10.29 * Math.pow(slp[kp], 2.0) * Math.pow(qpt[it][kp], 2.0) / Math.pow(dpl[kp], 5.333);
									Hwup[it][kp] = Hwdw[it][kp] + slopt[it][kp] * lp[kp];
									if (Hwup[it][kp] >= Hj[I0[kp]])
									{
										Hwup[it][kp] = Hj[I0[kp]];
										slopt[it][kp] = (Hwup[it][kp] - Hwdw[it][kp]) / lp[kp];
										if (slopt[it][kp] < 0.0)
										{
											slopt[it][kp] = Math.abs(slopt[it][kp]);
										}
										vpt[it][kp] = Math.pow(rid[it][kp], 0.6667) * Math.pow(slopt[it][kp], 0.5) / slp[kp];
										qqkp[it][kp] = vpt[it][kp] * Ad0;
										if (qqkp[it][kp] < 0.0)
										{
											qqkp[it][kp] = Math.abs(qqkp[it][kp]);
										}
									}
								}
								else
								{
									if (Iprt == 1)
									{
										printStream.println("   it= " + it + "   kp= " + kp + "  Hwdm= " + Hwdw[it][kp] + "  ����û�����ܳ��� ");
									}
									// ==20161018�޸Ŀ�ʼ---�����ٽ�ˮ��򻯹�ʽ--------zhou-p21------
									ycd0 = qpt[it][kp] / 2.983 / Math.pow(dpl[kp], 2.5);
									hdcc0[it][kp] = Math.pow(ycd0, 0.513);
									sita = 2.0 * Math.acos(1.0 - 2.0 * hdcc0[it][kp]);
									rid[it][kp] = 0.25 * dpl[kp] * (sita - Math.sin(sita)) / sita;
									vpt[it][kp] = Math.pow(rid[it][kp], 0.6667) * Math.pow(slop[kp], 0.5) / slp[kp];
								}
								// ---for(k=0;k<N;k++)����---20160907�޸Ľ���---�ٽ�ˮ���㷨--------------
								Hwdwkp = ZJdw[kp] + hdcc0[it][kp] * dpl[kp];
								if (Hwdwkp >= Hwdw[it][kp])
								{
									Hwdw[it][kp] = Hwdwkp;
								}
								else
								{
									yykp = Hwdw[it][kp] - ZJdw[kp];
									if (yykp > dpl[kp])
									{
										yykp = dpl[kp];
									}
									sita = 2.0 * Math.acos(1.0 - 2.0 * yykp / dpl[kp]);
									hdcc0[it][kp] = yykp / dpl[kp];
									rid[it][kp] = 0.25 * dpl[kp] * (sita - Math.sin(sita)) / sita;
									vpt[it][kp] = Math.pow(rid[it][kp], 0.6667) * Math.pow(slop[kp], 0.5) / slp[kp];
								}
								Hwup[it][kp] = Hwdw[it][kp] + slop[kp] * lp[kp];
							}
							// ------- ���it������ ----------
							if (Iprt == 1)
							{
								printStream.println("   it= " + it + "   kp= " + kp + "   I0[kp]= " + I0[kp] + "  Hwdm= " + Hwdw[it][kp] + "  Hwup= " + Hwup[it][kp] + "  Hj= " + Hj[I0[kp]] + "  hdcc0= " + hdcc0[it][kp] + "  qpt= " + qpt[it][kp] + "  qqkp= " + qqkp[it][kp] + "  vpt= " + vpt[it][kp]);
							}
						}
					}
				}
				printStream.println();

				printStream.println("    it   �ܶκ�  I0   J0 �ܾ�dpl     �ܶ�qp   ˮ���뾶R  ������ ����(m/s)  ����ˮλ  ����ˮλ  �Ϲܵ׸�  �¹ܵ׸�  �ܶ��¶�  �ϵ����");
				for (i = 0; i < NP; i++)
				{
					printStream.printf("%6d%6d%6d%5d%8.2f%12.3f%10.3f%8.3f%10.3f%10.3f%10.3f%10.3f%10.3f%10.5f%10.3f", it, i, I0[i], J0[i], dpl[i], qpt[it][i], rid[it][i], hdcc0[it][i], vpt[it][i], Hwup[it][i], Hwdw[it][i], ZJup[i], ZJdw[i], slop[i], Hj[I0[i]]);
					printStream.println();
				}
				printStream.println();
				// -------------- ��ʼ���������ڵ� ---------------
				for (i = 0; i < NP; i++)
				{
					k = J0[i];
					if (k == Nend)
					{
						Hwj[it][k] = Hwdw[it][i];
					}
					{
						j = I0[i];
						Hwj[it][j] = Hwup[it][i];
						if (Hwup[it][i] == Hj[j])
						{
							overflow[it][j] = overflow[it][j] + (qpt[it][i] - qqkp[it][i]) * dt * 60.0;
							Hw_over[it][j] = csf * overflow[it][j] / Aj[j] / 10000.0 * 1000.0;

						}
						if (Hwup[it][i] < Hj[j] && overflow[it][j] > 0.0)
						{
							overflow[it][j] = overflow[it - 1][j] * 0.90;
							Hw_over[it][j] = csf * overflow[it][j] / Aj[j] / 10000.0 * 1000.0;
						}
					}
					if (it > NR && Hw_over[it][j] <= 5.0)
					{
						overflow[it][j] = 0.0;
						Hw_over[it][j] = 0.0;
					}
				}
				// ------------------ ���������ڵ���� ---------------
			}
			// ----------------��Ļ����������------
			// System.out.println("------ ģ�ͼ���ȫ����� ------");
			// ---------------------- ����ܶγ����ȼ����� ---------------
			printStream.println(" ======== ʱ�ιܶγ����� ========");
			Nprt = NP / Nprtc + 1;
			for (ii = 0; ii < Nprt; ii++)
			{
				{
					iprt1 = ii * Nprtc;
					iprt2 = iprt1 + Nprtc;
					if (iprt2 > NP)
					{
						iprt2 = NP;
					}
				}
				printStream.print("  i=    ");
				for (i = iprt1; i < iprt2; i++)
				{
					if (i < 10)
					{
						printStream.print("    " + i + "   ");
					}
					else
					{
						printStream.print("   " + i + "   ");
					}
				}
				printStream.println();
				printStream.println();
				for (it = 0; it < NT; it++)
				{
					if (it < 10)
					{
						printStream.print(" " + it + "   ");
					}
					else
					{
						printStream.print(it + "   ");
					}
					for (i = iprt1; i < iprt2; i++)
					{
						printStream.printf("%8.3f", hdcc0[it][i]);
					}
					printStream.println();
				}
			}
			//
			// ------------------- ����ڵ�ˮλ������ ---------------
			printStream.println(" ======== ʱ�νڵ�ˮλ ========");
			Nprt = NN / Nprtc + 1;
			for (ii = 0; ii < Nprt; ii++)
			{
				{
					iprt1 = ii * Nprtc;
					iprt2 = iprt1 + Nprtc;
					if (iprt2 > NN)
					{
						iprt2 = NN;
					}
				}
				printStream.print("  i=    ");
				for (i = iprt1; i < iprt2; i++)
				{
					if (i < 10)
					{
						printStream.print("    " + i + "   ");
					}
					else
					{
						printStream.print("   " + i + "  ");
					}
				}
				printStream.println();
				printStream.println("it=");
				for (it = 0; it < NT; it++)
				{
					if (it < 10)
					{
						printStream.print(" " + it + "   ");
					}
					else
					{
						printStream.print(it + "   ");
					}
				}
			}

			// ***********��֯���ݣ�����ҳ��������ʾ********
			for (it = 0; it < NT; it++)
			{
				String WaterLevNew = "";
				for (i = 0; i < NN; i++)
				{
					if (gjId != null && i == SubgjId)
					{
						WaterAccGj += df1.format(Hwj[it][i]) + "|";
					}
					WaterLevNew += df1.format(Hwj[it][i]) + "|";
				}
				WaterLev[it] = WaterLevNew;
			}
			// *************************************
			// ------------------ ����ڵ����������� ---------------
			printStream.println(" ======== ʱ�νڵ��ˮ��(m3) ========");
			Nprt = NN / Nprtc + 1;
			for (ii = 0; ii < Nprt; ii++)
			{
				{
					iprt1 = ii * Nprtc;
					iprt2 = iprt1 + Nprtc;
					if (iprt2 > NN)
					{
						iprt2 = NN;
					}
				}
				printStream.print("  i=    ");
				for (i = iprt1; i < iprt2; i++)
				{
					if (i < 10)
					{
						printStream.print("    " + i + "   ");
					}
					else
					{
						printStream.print("   " + i + "  ");
					}
				}
				printStream.println();
				printStream.println("it=");
				for (it = 0; it < NT; it++)
				{
					if (it < 10)
					{
						printStream.println(" " + it + "   ");
					}
					else
					{
						printStream.println(it + "   ");
					}
					for (i = iprt1; i < iprt2; i++)
					{
						if (overflow[it][i] <= 0.0)
						{
							printStream.print("        ");
						}
						else
						{
							printStream.printf("%8.2f", overflow[it][i]);
						}
					}
					printStream.println();
					String WaterAccNew = "";
					for (i = 0; i < NN; i++)
					{
						if (overflow[it][i] <= 0.0)
						{
							WaterAccNew += 0 + "|";
						}
						else
						{
							WaterAccNew += df1.format(overflow[it][i]) + "|";
						}
					}
					WaterAcc[it] = WaterAccNew;
				}
			}

			// ***********��֯���ݣ�����ҳ��������ʾ********
			for (it = 0; it < NT; it++)
			{
				String WaterAccNew = "";
				for (i = 0; i < NN; i++)
				{
					if (overflow[it][i] <= 0.0)
					{
						WaterAccNew += 0 + "|";
					}
					else
					{
						WaterAccNew += df1.format(overflow[it][i]) + "|";
					}
				}
				WaterAcc[it] = WaterAccNew;
			}
			// *********************************************
			printStream.println(" ======== ʱ�νڵ��ˮ���(mm) ========");
			Nprt = NN / Nprtc + 1;
			for (ii = 0; ii < Nprt; ii++)
			{
				{
					iprt1 = ii * Nprtc;
					iprt2 = iprt1 + Nprtc;
					if (iprt2 > NN)
					{
						iprt2 = NN;
					}
				}
				printStream.print("  i=    ");
				for (i = iprt1; i < iprt2; i++)
				{
					if (i < 10)
					{
						printStream.print("    " + i + "   ");
					}
					else
					{
						printStream.print("   " + i + "   ");
					}
				}
				printStream.println();
				printStream.println("it=");
				for (it = 0; it < NT; it++)
				{
					if (it < 10)
					{
						printStream.print(" " + i + "   ");
					}
					else
					{
						printStream.print(i + "   ");
					}
					for (i = iprt1; i < iprt2; i++)
					{
						if (overflow[it][i] <= 0.0)
						{
							printStream.print("        ");
						}
						else
						{
							printStream.printf("%8.2f", Hw_over[it][i]);
						}
					}
					printStream.println();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return gjName;
		}
		if (AnalogWaterType.equals("WaterAccGj"))
		{
			return WaterAccGj;
		}
		else if (AnalogWaterType.equals("WaterAcc"))
		{
			String WaterAccList = "";
			for (int i = 0; i < WaterAcc.length; i++)
			{
				WaterAccList += subSys.substring(7, 12) + WaterAcc[i] + ";";
			}
			return WaterAccList;
		}
		else if (AnalogWaterType.equals("WaterLev"))
		{
			String WaterLevList = "";
			for (int i = 0; i < WaterLev.length; i++)
			{
				WaterLevList += subSys.substring(7, 12) + WaterLev[i] + ";";
			}
			return WaterLevList;
		}
		return "";
	}

	// �����װ汾
	// �ر�˵������һ�汾��ǰ�����汾���õı��һ��
	private String analog_Y3(String subSys, int timePeriod, String gjId, String AnalogWaterType)
	{
		int SubgjId = 0;
		if (gjId != null)
		{
			SubgjId = CommUtil.StrToInt(gjId.substring(12, 15)) - 1;
		}
		try
		{
			// �����������ݣ�
			// �ܶ������ڵ������ܵ��������·�����ܶ����������������ģ��ʱ������֥�Ӹ���ʱ��λ��
			// �ܵ�·������·�����ڵ������յ�ڵ�ţ��м�������ļ�ָ��
			int NP = 9, NN = 10, Nstart = 3, Npline = 7, NT = 60, NR = 23, Nroute = 3, Nr_node = 8, Nend = 7, Iprt = 0, Nprtc = 20;
			// ���깫ʽ����shanghai storm water formular:
			// (A1+C*lgP)/(t+b)**n---ln(N)=2.303log(N)---����ˮλ��m��
			// �ܶ����٣�m/s��, �ܶ��趨����vp0�����氼͹ϵ��csf
			double A1 = 17.53, C_storm = 0.95, tmin = 10, b_storm = 11.77, P_simu = 100, n_storm = 0.88, dt = 2.0, rc = 0.375, Hw_end = 4.1, vp0 = 0.8, csf = 3.0;

			// ��ϵͳ�ܶ����ݣ�
			int[] I0; // �ܶ����νڵ��I0,
			int[] J0; // ���νڵ��J0,
			double[] lp; // �ܶγ���
			double[] dpl; // �ܶ�ֱ��(m)
			double[] slp; // Ħ|��ϵ��
			double[] ZJup; // ���ιܵ׸߳�(m)
			double[] ZJdw; // ���ιܵ׸߳�(m)

			// ��ϵͳ�ڵ�����
			// ������ʼ�ڵ�ź���ʼ�ڵ�ܵ�����<m>
			double[] Aj; // �ڵ��ˮ���(ha)3.5
			double[] Acoef; // �ڵ��ˮ�������ϵ��0.6
			double[] Hj; // �ڵ�����ߣ�m��[NN=23]

			// ����·������·���ڵ��(-99��ʾ�սڵ�)
			int[][] Mroute;

			// ����·������
			int[] Mstart;

			// ��ϵͳ��֧·���ܶ����ݾ��� ����pipe branches-reverse order
			int[][] Mbranch;

			String XlsPath = "";
			if (gjId != null)
			{
				XlsPath = FileSaveRoute + gjId.substring(0, 12) + ".xls";
				gjName = gjId.substring(0, 12);
			}
			else
			{
				XlsPath = FileSaveRoute + subSys + ".xls";
				gjName = subSys;
			}
			InputStream is = new FileInputStream(XlsPath);
			Workbook rwb = Workbook.getWorkbook(is);
			Sheet rs = rwb.getSheet(0);
			int rsRows = rs.getRows();

			/*
			 * �������ݱ����ϵͳ�� �ڵ���NN �ܶ���NP �����NStart ·���ܶ���Npline ·���ڵ���Nr_node
			 * �յ���ں�Nend ģ��ʱ��NT �ܶ�·����NrouteYJ002 10 9 3 7 8 8 60 3
			 */
			int rowCnt = 2;
			String sysName = rs.getCell(0, rowCnt).getContents().trim();
			NN = Integer.parseInt(rs.getCell(1, rowCnt).getContents().trim());
			NP = Integer.parseInt(rs.getCell(2, rowCnt).getContents().trim());
			Nstart = Integer.parseInt(rs.getCell(3, rowCnt).getContents().trim());
			Npline = Integer.parseInt(rs.getCell(4, rowCnt).getContents().trim());
			Nr_node = Integer.parseInt(rs.getCell(5, rowCnt).getContents().trim());
			Nend = Integer.parseInt(rs.getCell(6, rowCnt).getContents().trim());
			NT = Integer.parseInt(rs.getCell(7, rowCnt).getContents().trim());
			Nroute = Integer.parseInt(rs.getCell(8, rowCnt).getContents().trim());
			rowCnt += 4;

			/*
			 * ��ϵͳ�ܶ����ݱ�� Pipe.No ����I0 �յ��J0 ����LP ֱ��DP Ħ��ϵ�� ��˱�� �ն˱�� 1 0 1 28.5
			 * 0.3 0.017 3.894 3.842 2 1 2 32 0.3 0.017 3.842 3.784 3 2 3 28.6
			 * 0.3 0.017 3.784 3.733 4 3 4 25.4 0.3 0.017 3.733 3.687 5 4 5 24.7
			 * 0.3 0.017 3.687 3.643 6 5 6 23.5 0.3 0.017 3.643 3.601 7 6 7 30.4
			 * 0.3 0.017 3.601 3.546 8 8 7 15.5 0.3 0.017 3.731 3.171 9 9 6 4.3
			 * 0.3 0.017 3.886 3.7
			 */
			I0 = new int[NP];
			J0 = new int[NP];
			lp = new double[NP];
			dpl = new double[NP];
			slp = new double[NP];
			ZJup = new double[NP];
			ZJdw = new double[NP];
			for (int j = 0; j < NP; j++)
			{
				I0[j] = Integer.parseInt(rs.getCell(1, rowCnt + j).getContents().trim());
				J0[j] = Integer.parseInt(rs.getCell(2, rowCnt + j).getContents().trim());
				lp[j] = Double.parseDouble(rs.getCell(3, rowCnt + j).getContents().trim());
				dpl[j] = Double.parseDouble(rs.getCell(4, rowCnt + j).getContents().trim());
				slp[j] = Double.parseDouble(rs.getCell(5, rowCnt + j).getContents().trim());
				ZJup[j] = Double.parseDouble(rs.getCell(6, rowCnt + j).getContents().trim());
				ZJdw[j] = Double.parseDouble(rs.getCell(7, rowCnt + j).getContents().trim());
			}
			rowCnt += NP;
			rowCnt += 3;

			/*
			 * ��ϵͳ�ڵ����ݱ��ڵ�No ��ˮ���ha ����ϵ�� ������ ���ױ�� 1 3.5 0.6 5.244 ��δ�õ� 2 3.5
			 * 0.6 5.191 3 3.5 0.6 5.177 4 3.5 0.6 5.208 5 3.5 0.6 5.221 6 3.5
			 * 0.6 5.201 7 3.5 0.6 5.2 8 3.5 0.6 5.121 9 3.5 0.6 5.131 10 3.5
			 * 0.6 5.186
			 */
			Aj = new double[NN];
			Acoef = new double[NN];
			Hj = new double[NN];
			for (int j = 0; j < NN; j++)
			{
				Aj[j] = Double.parseDouble(rs.getCell(1, rowCnt + j).getContents().trim());
				Acoef[j] = Double.parseDouble(rs.getCell(2, rowCnt + j).getContents().trim());
				Hj[j] = Double.parseDouble(rs.getCell(3, rowCnt + j).getContents().trim());
			}
			rowCnt += NN;
			rowCnt += 3;

			// **************����һ�汾��ȥ��**********
			/**
			 * ����·����&·���ڵ�Žڵ���� 1 2 3 4 5 6 7 8 1 0 1 2 3 4 5 6 7 2 8 7 -99 -99
			 * -99 -99 -99 -99 3 9 6 -99 -99 -99 -99 -99 -99
			 */
			/**
			 * Mroute = new int[Nstart][Nr_node]; for (int j = 0; j < Nstart;
			 * j++) { for (int k = 0; k < Nr_node; k++) { Mroute[j][k] =
			 * Integer.parseInt(rs.getCell(k + 1, rowCnt +
			 * j).getContents().trim()); } } rowCnt += Nstart; rowCnt += 3;
			 */
			// *******************************
			Mroute = new int[Nstart][Nr_node];
			// *************��һ�汾���¼�������********
			/**
			 * ����·������ ��� 1 2 3 ���� 0 8 9
			 */
			Mstart = new int[Nstart];
			for (int j = 0; j < Nstart; j++)
			{
				Mstart[j] = Integer.parseInt(rs.getCell(j + 1, rowCnt).getContents().trim());
			}
			rowCnt += 1;
			rowCnt += 3;
			// ************************************

			/*
			 * ��ϵͳ��֧·���ܶ����ݾ��� ����pipe branches-reverse order �ڵ���� 1 2 3 4 5 6 7 1
			 * 6 5 4 3 2 1 0 2 7 -99 -99 -99 -99 -99 -99 3 8 -99 -99 -99 -99 -99
			 * -99
			 */
			Mbranch = new int[Nstart][Npline];
			for (int j = 0; j < Nstart; j++)
			{
				for (int k = 0; k < Npline; k++)
				{
					Mbranch[j][k] = Integer.parseInt(rs.getCell(k + 1, rowCnt + j).getContents().trim());
				}
			}
			// ----�ٽ�ˮ��������----
			double sita0 = 3.0, eps = 0.001, alfa = 0.5;
			double Ad0, qkpmax, Hwdwkp, yykp, sita, cons_b, sita_s = 0, sita_c, fsita, dfdsita, dfsita, ssita = 0, csita = 0, hyd_A, hafsita, shafsita = 0, chafsita, sita_p = 0;
			// �м����
			int i, j, k, ik, jk, it, k1, kp, in1, in2, in3, NR1, NR2, ii, Nprt, iprt1, iprt2;
			double H00, ycd0;
			double dtnt, taa, tbb, AA, XX1, XX2, hdj0;
			double[] XX = new double[NT];
			double[] qit = new double[NT];
			double[][] sumqj = new double[NT][NN];
			double[][] sumAj = new double[NT][NN];
			double[][] Tnode = new double[NN][NN];
			double[][] sumTnode = new double[NN][NN];
			double[] vp = new double[NP];
			double[] slop = new double[NP];
			double[][] qpt = new double[NT][NP];
			double[][] qqkp = new double[NT][NP];
			double[][] vpt = new double[NT][NP];
			double[][] rid = new double[NT][NP];
			double[][] slopt = new double[NT][NP];
			double[][] Hwup = new double[NT][NP];
			double[][] Hwdw = new double[NT][NP];
			double[][] hdcc0 = new double[NT][NP];
			double[][] overflow = new double[NT][NN];
			double[][] Hw_over = new double[NT][NN];
			double[][] Hwj = new double[NT][NN];

			// ----------------------------------------------------------------------------------------------------------
			String FileName = "";
			if (gjId != null)
			{
				FileName = gjId.substring(0, 12) + ".txt";
			}
			else
			{
				FileName = subSys + ".txt";
			}
			FileOutputStream fs = new FileOutputStream(new File(FilePath + FileName));
			PrintStream printStream = new PrintStream(fs);
			printStream.println(FileName);

			DecimalFormat df = new DecimalFormat("##.####");
			DecimalFormat df1 = new DecimalFormat("######.##");
			// --��������ļ���ʼ---
			// ================= ����ֵ ===============================
			for (i = 0; i < NT; i++)
			{
				for (j = 0; j < NN; j++)
					sumAj[i][j] = 0;
			}
			for (i = 0; i < NT; i++)
			{
				for (j = 0; j < NN; j++)
					sumqj[i][j] = 0;
			}
			for (i = 0; i < NN; i++)
			{
				for (j = 0; j < NN; j++)
				{
					if (i == j)
					{
						Tnode[i][j] = 0;
					}
					else
					{
						Tnode[i][j] = -99;
					}
				}
			}
			for (i = 0; i < NN; i++)
			{
				for (j = 0; j < NN; j++)
					sumTnode[i][j] = 0;
			}
			//
			// =====20161029===== ���ɾ��� Mroute[i][j] ====
			//
			for (i = 0; i < Nstart; i++)
			{
				for (j = 0; j < Nr_node; j++)
					Mroute[i][j] = -99;
			}
			for (i = 0; i < Nstart; i++)
				Mroute[i][0] = Mstart[i];
			for (i = 0; i < Nstart; i++)
			{
				for (j = 1; j < Nr_node; j++)
				{
					for (k = 0; k < NP; k++)
					{
						if (I0[k] == Mroute[i][j - 1])
						{
							Mroute[i][j] = J0[k];
						}
					}
				}
			}
			// ==================Tnode-sumTnode=========================
			for (i = 0; i < NP; i++)
				vp[i] = vp0;
			for (kp = 0; kp < NP; kp++)
			{
				in1 = I0[kp];
				in2 = J0[kp];
				Tnode[in1][in2] = lp[kp] / vp[kp] / 60;
				slop[kp] = (ZJup[kp] - ZJdw[kp]) / lp[kp];
			}
			for (i = 0; i < Nstart; i++)
			{
				for (j = 0; j < Nr_node; j++)
				{
					in1 = Mroute[i][j];
					if (in1 >= 0)
					{
						for (k = j + 1; k < Nr_node; k++)
						{
							in2 = Mroute[i][k - 1];
							in3 = Mroute[i][k];
							if (in3 >= 0)
							{
								sumTnode[in1][in3] = sumTnode[in1][in2] + Tnode[in2][in3];
							}
						}
					}
				}
			}
			// System.out.println("pipe no.  I0    J0");
			for (i = 0; i < NP; i++)
			{
				// System.out.printf("%6d%6d%6d", i, I0[i], J0[i]);
				// System.out.println();
			}
			printStream.println();
			printStream.print(" ip=");
			for (i = 0; i < NP; i++)
			{
				printStream.printf("%4d", i);
			}
			printStream.println();
			printStream.print(" I0=");
			for (i = 0; i < NP; i++)
			{
				printStream.printf("%4d", I0[i]);
			}
			printStream.println();
			printStream.print(" J0=");
			for (i = 0; i < NP; i++)
			{
				printStream.printf("%4d", J0[i]);
			}
			printStream.println();
			printStream.println();
			printStream.println("===========  print Mroute[i][j]");
			for (i = 0; i < Nstart; i++)
			{
				for (j = 0; j < Nr_node; j++)
				{
					printStream.printf("%6d", Mroute[i][j]);
				}
				printStream.println();
			}
			printStream.println();
			printStream.println("===========  print Mbranch[i][j]");
			for (i = 0; i < Nstart; i++)
			{
				for (j = 0; j < Npline; j++)
				{
					printStream.printf("%6d", Mbranch[i][j]);
				}
				printStream.println();
			}
			printStream.println("===========  print Tnode[i][j]");
			printStream.println("====j=  ");
			printStream.println("      ");
			for (j = 0; j < NN; j++)
			{
				printStream.printf("%6d", j);
			}
			printStream.println();
			for (i = 0; i < NN; i++)
			{
				if (i < 10)
				{
					printStream.print("i=" + i + "   ");
				}
				else
				{
					printStream.print("i=" + i + "  ");
				}
				for (j = 0; j < NN; j++)
				{
					if (Tnode[i][j] < 0.0)
					{
						printStream.print("      ");
					}
					else
					{
						printStream.printf("%6.2f", Tnode[i][j]);
					}
				}
				printStream.println();
			}
			printStream.println();
			printStream.println("===========  print sumTnode[i][j]");
			printStream.print("==j=  ");
			for (j = 0; j < NN; j++)
			{
				printStream.printf("%6d", j);
			}
			printStream.println();
			for (i = 0; i < NN; i++)
			{
				printStream.print("i=" + i + "   ");
				for (j = 0; j < NN; j++)
				{
					if (sumTnode[i][j] <= 0.0)
					{
						printStream.print("      ");
					}
					else
					{
						printStream.printf("%6.2f", sumTnode[i][j]);
					}
				}
				printStream.println();
			}
			// ================= ����׼��̬����ģ��============================
			// -------------------��̬ģ����������-----------------------------
			// ----------------�ڵ��ˮ���(ha)�ͻ�ˮ����(m3/sec)����--------
			printStream.println();
			printStream.println("===========  ������̬ģ�����      �����ڣ� " + P_simu + "  ��   ʱ������ " + NT + "       �յ�ˮλ�� " + Hw_end + "  m  =========");
			// ֥�Ӹ������--rainfall intensity at every time step--
			AA = A1 + A1 * C_storm * Math.log(P_simu) / 2.303;
			for (it = 0; it < NT; it++)
			{
				if (it <= NR)
				{
					dtnt = dt * (float) (it);
					tbb = dt * (float) (NR) - dtnt;
					XX1 = AA * ((1.0 - n_storm) * tbb / rc + b_storm);
					XX2 = Math.pow((tbb / rc + b_storm), (n_storm + 1.0));
				}
				else
				{
					dtnt = dt * (float) (it);
					taa = dtnt - dt * (float) (NR);
					XX1 = AA * ((1.0 - n_storm) * taa / (1.0 - rc) + b_storm);
					XX2 = Math.pow((taa / (1.0 - rc) + b_storm), (n_storm + 1.0));
				}
				XX[it] = XX1 / XX2;
				qit[it] = 167.0 * XX[it] / 1000.0;
			}
			NR1 = NR - 1;
			NR2 = NR + 1;
			qit[NR] = (qit[NR] + qit[NR - 1] + qit[NR + 1]) / 3.0;
			printStream.println();
			printStream.println("    it      dtnt      XX[it]     qit[it]");
			for (it = 0; it < NT; it++)
			{
				dtnt = dt * (float) (it);
				printStream.printf("%6d%10.2f%12.6f%12.6f", it, dtnt, XX[it], qit[it]);
				printStream.println();
			}
			printStream.println();
			for (it = 0; it < NT; it++)
			{
				dtnt = dt + dt * (float) (it);
				for (j = 0; j < NN; j++)
				{
					sumAj[it][j] = Aj[j];
					sumqj[it][j] = Aj[j] * qit[it] * Acoef[j];
					for (i = 0; i < NN; i++)
					{
						if (sumTnode[i][j] > 0 && sumTnode[i][j] < dtnt)
						{
							sumAj[it][j] = sumAj[it][j] + Aj[i];
							sumqj[it][j] = sumqj[it][j] + Aj[i] * qit[it] * Acoef[i];
						}
					}
				}
			}
			printStream.println("  sumAj[it][j]=");
			for (it = 0; it < NT; it++)
			{
				for (j = 0; j < NN; j++)
				{
					printStream.printf("%8.2f", sumAj[it][j]);
				}
				printStream.println();
			}
			printStream.println();
			printStream.println("  sumqj[it][j]=");
			for (it = 0; it < NT; it++)
			{
				for (j = 0; j < NN; j++)
				{
					printStream.printf("%8.2f", sumqj[it][j]);
				}
				printStream.println();
			}
			printStream.println();
			// ---------------------------------------------------------------
			for (it = 0; it < NT; it++)
			{
				for (i = 0; i < NN; i++)
				{
					overflow[it][i] = 0.0;
					Hw_over[it][i] = 0.0;
				}
			}
			for (it = 0; it < NT; it++)
			{
				for (j = 0; j < NP; j++)
				{
					qpt[it][j] = -99.0;
					qqkp[it][j] = 0.0;
				}
			}
			// ---------------------------------------------------------------
			for (it = 0; it < NT; it++)
			{
				printStream.print(" it=" + it + "  qpt[it][k]=");
				for (j = 0; j < NN; j++)
				{
					for (k = 0; k < NP; k++)
					{
						if (I0[k] == j)
						{
							qpt[it][k] = sumqj[it][j];
							printStream.printf("%8.2f", qpt[it][k]);
						}
					}
				}
				printStream.println();
				for (ik = 0; ik < Nstart; ik++)
				{
					for (jk = 0; jk < Npline; jk++)
					{
						kp = Mbranch[ik][jk];
						if (kp >= 0)
						{
							if (J0[kp] == Nend)
							{
								Hwdw[it][kp] = Hw_end;
								if (1 == Iprt)
								{
									printStream.println("   it= " + it + "   kp= " + kp + "  Hwdm= " + Hwdw[it][kp] + "  Hw_end= " + Hw_end);
								}
							}
							else
							{
								for (k1 = 0; k1 < NP; k1++)
								{
									if (I0[k1] == J0[kp]) Hwdw[it][kp] = Hwup[it][k1];
								}
							}
							Ad0 = 0.7854 * Math.pow(dpl[kp], 2.0);
							hdj0 = ZJdw[kp] + dpl[kp];
							if (Hwdw[it][kp] >= hdj0)
							{
								if (1 == Iprt)
								{
									printStream.println("   it= " + it + "   kp= " + kp + "  Hwdm= " + df.format(Hwdw[it][kp]) + "  ��û���� ");
								}
								hdcc0[it][kp] = 1.0;
								rid[it][kp] = dpl[kp] / 4.0;
								vpt[it][kp] = qpt[it][kp] / Ad0;
								slopt[it][kp] = 10.29 * Math.pow(slp[kp], 2.0) * Math.pow(qpt[it][kp], 2.0) / Math.pow(dpl[kp], 5.333);
								Hwup[it][kp] = Hwdw[it][kp] + slopt[it][kp] * lp[kp];
								if (Hwup[it][kp] >= Hj[I0[kp]])
								{
									Hwup[it][kp] = Hj[I0[kp]];
									slopt[it][kp] = (Hwup[it][kp] - Hwdw[it][kp]) / lp[kp];
									if (slopt[it][kp] < 0.0)
									{
										slopt[it][kp] = Math.abs(slopt[it][kp]);
									}
									vpt[it][kp] = Math.pow(rid[it][kp], 0.6667) * Math.pow(slopt[it][kp], 0.5) / slp[kp];
									qqkp[it][kp] = vpt[it][kp] * Ad0;
									if (qqkp[it][kp] < 0.0)
									{
										qqkp[it][kp] = Math.abs(qqkp[it][kp]);
									}
								}
							}
							else
							{
								if (Iprt == 1)
								{
									printStream.println("   it= " + it + "   kp= " + kp + "  Hwdw= " + Hwdw[it][kp] + "  ����û���� ");
								}
								// --20161018�޸Ŀ�ʼ---�����ٽ�ˮ����㷨-----------------------
								qkpmax = 2.699 * Math.pow(dpl[kp], 2.5);
								if (qpt[it][kp] > qkpmax)
								{
									if (Iprt == 1)
									{
										printStream.println("   it= " + it + "   kp= " + kp + "  qkpmax= " + qkpmax + "  ����û���ܳ��� ");
									}
									vpt[it][kp] = qpt[it][kp] / Ad0;
									H00 = Math.pow(vpt[it][kp], 2.0) / 13.72;
									Hwdw[it][kp] = ZJdw[kp] + dpl[kp] + H00;
									hdcc0[it][kp] = 1.0;
									rid[it][kp] = dpl[kp] / 4.0;
									slopt[it][kp] = 10.29 * Math.pow(slp[kp], 2.0) * Math.pow(qpt[it][kp], 2.0) / Math.pow(dpl[kp], 5.333);
									Hwup[it][kp] = Hwdw[it][kp] + slopt[it][kp] * lp[kp];
									if (Hwup[it][kp] >= Hj[I0[kp]])
									{
										Hwup[it][kp] = Hj[I0[kp]];
										slopt[it][kp] = (Hwup[it][kp] - Hwdw[it][kp]) / lp[kp];
										if (slopt[it][kp] < 0.0)
										{
											slopt[it][kp] = Math.abs(slopt[it][kp]);
										}
										vpt[it][kp] = Math.pow(rid[it][kp], 0.6667) * Math.pow(slopt[it][kp], 0.5) / slp[kp];
										qqkp[it][kp] = vpt[it][kp] * Ad0;
										if (qqkp[it][kp] < 0.0)
										{
											qqkp[it][kp] = Math.abs(qqkp[it][kp]);
										}
									}
								}
								else
								{
									if (Iprt == 1)
									{
										printStream.println("   it= " + it + "   kp= " + kp + "  Hwdm= " + Hwdw[it][kp] + "  ����û�����ܳ��� ");
									}
									// ==20161018�޸Ŀ�ʼ---�����ٽ�ˮ��򻯹�ʽ--------zhou-p21------
									ycd0 = qpt[it][kp] / 2.983 / Math.pow(dpl[kp], 2.5);
									hdcc0[it][kp] = Math.pow(ycd0, 0.513);
									sita = 2.0 * Math.acos(1.0 - 2.0 * hdcc0[it][kp]);
									rid[it][kp] = 0.25 * dpl[kp] * (sita - Math.sin(sita)) / sita;
									vpt[it][kp] = Math.pow(rid[it][kp], 0.6667) * Math.pow(slop[kp], 0.5) / slp[kp];
								}
								// ---for(k=0;k<N;k++)����---20160907�޸Ľ���---�ٽ�ˮ���㷨--------------
								Hwdwkp = ZJdw[kp] + hdcc0[it][kp] * dpl[kp];
								if (Hwdwkp >= Hwdw[it][kp])
								{
									Hwdw[it][kp] = Hwdwkp;
								}
								else
								{
									yykp = Hwdw[it][kp] - ZJdw[kp];
									if (yykp > dpl[kp])
									{
										yykp = dpl[kp];
									}
									sita = 2.0 * Math.acos(1.0 - 2.0 * yykp / dpl[kp]);
									hdcc0[it][kp] = yykp / dpl[kp];
									rid[it][kp] = 0.25 * dpl[kp] * (sita - Math.sin(sita)) / sita;
									vpt[it][kp] = Math.pow(rid[it][kp], 0.6667) * Math.pow(slop[kp], 0.5) / slp[kp];
								}
								Hwup[it][kp] = Hwdw[it][kp] + slop[kp] * lp[kp];
							}
							// ------- ���it������ ----------
							if (Iprt == 1)
							{
								printStream.println("   it= " + it + "   kp= " + kp + "   I0[kp]= " + I0[kp] + "  Hwdm= " + Hwdw[it][kp] + "  Hwup= " + Hwup[it][kp] + "  Hj= " + Hj[I0[kp]] + "  hdcc0= " + hdcc0[it][kp] + "  qpt= " + qpt[it][kp] + "  qqkp= " + qqkp[it][kp] + "  vpt= " + vpt[it][kp]);
							}
						}
					}
				}
				printStream.println();

				printStream.println("    it   �ܶκ�  I0   J0 �ܾ�dpl     �ܶ�qp   ˮ���뾶R  ������ ����(m/s)  ����ˮλ  ����ˮλ  �Ϲܵ׸�  �¹ܵ׸�  �ܶ��¶�  �ϵ����");
				for (i = 0; i < NP; i++)
				{
					printStream.printf("%6d%6d%6d%5d%8.2f%12.3f%10.3f%8.3f%10.3f%10.3f%10.3f%10.3f%10.3f%10.5f%10.3f", it, i, I0[i], J0[i], dpl[i], qpt[it][i], rid[it][i], hdcc0[it][i], vpt[it][i], Hwup[it][i], Hwdw[it][i], ZJup[i], ZJdw[i], slop[i], Hj[I0[i]]);
					printStream.println();
				}
				printStream.println();
				// -------------- ��ʼ���������ڵ� ---------------
				for (i = 0; i < NP; i++)
				{
					k = J0[i];
					if (k == Nend)
					{
						Hwj[it][k] = Hwdw[it][i];
					}
					{
						j = I0[i];
						Hwj[it][j] = Hwup[it][i];
						if (Hwup[it][i] == Hj[j])
						{
							overflow[it][j] = overflow[it][j] + (qpt[it][i] - qqkp[it][i]) * dt * 60.0;
							Hw_over[it][j] = csf * overflow[it][j] / Aj[j] / 10000.0 * 1000.0;

						}
						if (Hwup[it][i] < Hj[j] && overflow[it][j] > 0.0)
						{
							overflow[it][j] = overflow[it - 1][j] * 0.90;
							Hw_over[it][j] = csf * overflow[it][j] / Aj[j] / 10000.0 * 1000.0;
						}
					}
					if (it > NR && Hw_over[it][j] <= 5.0)
					{
						overflow[it][j] = 0.0;
						Hw_over[it][j] = 0.0;
					}
				}
				// ------------------ ���������ڵ���� ---------------
			}
			// ----------------��Ļ����������------
			// System.out.println("------ ģ�ͼ���ȫ����� ------");
			// ---------------------- ����ܶγ����ȼ����� ---------------
			printStream.println(" ======== ʱ�ιܶγ����� ========");
			Nprt = NP / Nprtc + 1;
			for (ii = 0; ii < Nprt; ii++)
			{
				{
					iprt1 = ii * Nprtc;
					iprt2 = iprt1 + Nprtc;
					if (iprt2 > NP)
					{
						iprt2 = NP;
					}
				}
				printStream.print("  i=    ");
				for (i = iprt1; i < iprt2; i++)
				{
					if (i < 10)
					{
						printStream.print("    " + i + "   ");
					}
					else
					{
						printStream.print("   " + i + "   ");
					}
				}
				printStream.println();
				printStream.println();
				for (it = 0; it < NT; it++)
				{
					if (it < 10)
					{
						printStream.print(" " + it + "   ");
					}
					else
					{
						printStream.print(it + "   ");
					}
					for (i = iprt1; i < iprt2; i++)
					{
						printStream.printf("%8.3f", hdcc0[it][i]);
					}
					printStream.println();
				}
			}
			//
			// ------------------- ����ڵ�ˮλ������ ---------------
			printStream.println(" ======== ʱ�νڵ�ˮλ ========");
			Nprt = NN / Nprtc + 1;
			for (ii = 0; ii < Nprt; ii++)
			{
				{
					iprt1 = ii * Nprtc;
					iprt2 = iprt1 + Nprtc;
					if (iprt2 > NN)
					{
						iprt2 = NN;
					}
				}
				printStream.print("  i=    ");
				for (i = iprt1; i < iprt2; i++)
				{
					if (i < 10)
					{
						printStream.print("    " + i + "   ");
					}
					else
					{
						printStream.print("   " + i + "  ");
					}
				}
				printStream.println();
				printStream.println("it=");
				for (it = 0; it < NT; it++)
				{
					if (it < 10)
					{
						printStream.print(" " + it + "   ");
					}
					else
					{
						printStream.print(it + "   ");
					}
				}
			}

			// ***********��֯���ݣ�����ҳ��������ʾ********
			for (it = 0; it < NT; it++)
			{
				String WaterLevNew = "";
				for (i = 0; i < NN; i++)
				{
					if (gjId != null && i == SubgjId)
					{
						WaterAccGj += df1.format(Hwj[it][i]) + "|";
					}
					WaterLevNew += df1.format(Hwj[it][i]) + "|";
				}
				WaterLev[it] = WaterLevNew;
			}
			// *************************************
			// ------------------ ����ڵ����������� ---------------
			printStream.println(" ======== ʱ�νڵ��ˮ��(m3) ========");
			Nprt = NN / Nprtc + 1;
			for (ii = 0; ii < Nprt; ii++)
			{
				{
					iprt1 = ii * Nprtc;
					iprt2 = iprt1 + Nprtc;
					if (iprt2 > NN)
					{
						iprt2 = NN;
					}
				}
				printStream.print("  i=    ");
				for (i = iprt1; i < iprt2; i++)
				{
					if (i < 10)
					{
						printStream.print("    " + i + "   ");
					}
					else
					{
						printStream.print("   " + i + "  ");
					}
				}
				printStream.println();
				printStream.println("it=");
				for (it = 0; it < NT; it++)
				{
					if (it < 10)
					{
						printStream.println(" " + it + "   ");
					}
					else
					{
						printStream.println(it + "   ");
					}
					for (i = iprt1; i < iprt2; i++)
					{
						if (overflow[it][i] <= 0.0)
						{
							printStream.print("        ");
						}
						else
						{
							printStream.printf("%8.2f", overflow[it][i]);
						}
					}
					printStream.println();
					String WaterAccNew = "";
					for (i = 0; i < NN; i++)
					{
						if (overflow[it][i] <= 0.0)
						{
							WaterAccNew += 0 + "|";
						}
						else
						{
							WaterAccNew += df1.format(overflow[it][i]) + "|";
						}
					}
					WaterAcc[it] = WaterAccNew;
				}
			}

			// ***********��֯���ݣ�����ҳ��������ʾ********
			for (it = 0; it < NT; it++)
			{
				String WaterAccNew = "";
				for (i = 0; i < NN; i++)
				{
					if (overflow[it][i] <= 0.0)
					{
						WaterAccNew += 0 + "|";
					}
					else
					{
						WaterAccNew += df1.format(overflow[it][i]) + "|";
					}
				}
				WaterAcc[it] = WaterAccNew;
			}
			// *********************************************
			printStream.println(" ======== ʱ�νڵ��ˮ���(mm) ========");
			Nprt = NN / Nprtc + 1;
			for (ii = 0; ii < Nprt; ii++)
			{
				{
					iprt1 = ii * Nprtc;
					iprt2 = iprt1 + Nprtc;
					if (iprt2 > NN)
					{
						iprt2 = NN;
					}
				}
				printStream.print("  i=    ");
				for (i = iprt1; i < iprt2; i++)
				{
					if (i < 10)
					{
						printStream.print("    " + i + "   ");
					}
					else
					{
						printStream.print("   " + i + "   ");
					}
				}
				printStream.println();
				printStream.println("it=");
				for (it = 0; it < NT; it++)
				{
					if (it < 10)
					{
						printStream.print(" " + i + "   ");
					}
					else
					{
						printStream.print(i + "   ");
					}
					for (i = iprt1; i < iprt2; i++)
					{
						if (overflow[it][i] <= 0.0)
						{
							printStream.print("        ");
						}
						else
						{
							printStream.printf("%8.2f", Hw_over[it][i]);
						}
					}
					printStream.println();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return gjName;
		}
		if (AnalogWaterType.equals("WaterAccGj"))
		{
			return WaterAccGj;
		}
		else if (AnalogWaterType.equals("WaterAcc"))
		{
			String WaterAccList = "";
			for (int i = 0; i < WaterAcc.length; i++)
			{
				WaterAccList += subSys.substring(7, 12) + WaterAcc[i] + ";";
			}
			return WaterAccList;
		}
		else if (AnalogWaterType.equals("WaterLev"))
		{
			String WaterLevList = "";
			for (int i = 0; i < WaterLev.length; i++)
			{
				WaterLevList += subSys.substring(7, 12) + WaterLev[i] + ";";
			}
			return WaterLevList;
		}
		return "";
	}

	// �����װ汾
	// �ر�˵������һ�汾��ǰ�����汾���õı��һ��
	private String analog_Y4(String subSys, int timePeriod, String gjId, String AnalogWaterType)
	{
		int SubgjId = 0;
		if (gjId != null)
		{
			SubgjId = CommUtil.StrToInt(gjId.substring(12, 15)) - 1;
		}
		try
		{
			// �����������ݣ�
			// �ܶ������ڵ������ܵ��������·�����ܶ����������������ģ��ʱ������֥�Ӹ���ʱ��λ��
			// �ܵ�·������·�����ڵ������յ�ڵ�ţ��м�������ļ�ָ��
			int NP = 9, NN = 10, Nstart = 3, Npline = 7, NT = 60, NR = 23, Nr_node = 8, Nend = 7, Iprt = 0, Nprtc = 20;
			int Ncol = 5;// �ڵ��������ܶ���+3���˴���С

			// ���깫ʽ����shanghai storm water formular:
			// (A1+C*lgP)/(t+b)**n---ln(N)=2.303log(N)---����ˮλ��m��
			// �ܶ����٣�m/s��, �ܶ��趨����vp0�����氼͹ϵ��csf
			double A1 = 17.53, C_storm = 0.95, b_storm = 11.77, P_simu = 10, n_storm = 0.88, dt = 2.0, rc = 0.375, Hw_end = 4.2, vp0 = 0.8, csf = 1.0;

			// ��ϵͳ�ܶ����ݣ�
			int[] I0; // �ܶ����νڵ��I0,
			int[] J0; // ���νڵ��J0,
			double[] lp; // �ܶγ���
			double[] dpl; // �ܶ�ֱ��(m)
			double[] slp; // Ħ|��ϵ��
			double[] ZJup; // ���ιܵ׸߳�(m)
			double[] ZJdw; // ���ιܵ׸߳�(m)

			// ��ϵͳ�ڵ�����
			// ������ʼ�ڵ�ź���ʼ�ڵ�ܵ�����<m>
			double[] Aj; // �ڵ��ˮ���(ha)3.5
			double[] Acoef; // �ڵ��ˮ�������ϵ��0.6
			double[] Hj; // �ڵ�����ߣ�m��[NN=23]

			// ����·������·���ڵ��(-99��ʾ�սڵ�)
			int[][] Mroute;

			// ����·������
			int[] Mstart;

			// ��ϵͳ��֧·���ܶ����ݾ��� ����pipe branches-reverse order
			int[][] Mbranch;

			String XlsPath = "";
			if (gjId != null)
			{
				XlsPath = FileSaveRoute + gjId.substring(0, 12) + ".xls";
				gjName = gjId.substring(0, 12);
			}
			else
			{
				XlsPath = FileSaveRoute + subSys + ".xls";
				gjName = subSys;
			}
			InputStream is = new FileInputStream(XlsPath);
			Workbook rwb = Workbook.getWorkbook(is);
			Sheet rs = rwb.getSheet(0);
			int rsRows = rs.getRows();

			/*
			 * �������ݱ����ϵͳ�� �ڵ���NN �ܶ���NP �����NStart ·���ܶ���Npline ·���ڵ���Nr_node
			 * �յ���ں�Nend ģ��ʱ��NT �ܶ�·����NrouteYJ002 10 9 3 7 8 8 60 3
			 */
			int rowCnt = 2;
			String sysName = rs.getCell(0, rowCnt).getContents().trim();
			NN = Integer.parseInt(rs.getCell(1, rowCnt).getContents().trim());
			NP = Integer.parseInt(rs.getCell(2, rowCnt).getContents().trim());
			Nstart = Integer.parseInt(rs.getCell(3, rowCnt).getContents().trim());
			Npline = Integer.parseInt(rs.getCell(4, rowCnt).getContents().trim());
			Nr_node = Integer.parseInt(rs.getCell(5, rowCnt).getContents().trim());
			Nend = Integer.parseInt(rs.getCell(6, rowCnt).getContents().trim());
			NT = Integer.parseInt(rs.getCell(7, rowCnt).getContents().trim());
			Ncol = Integer.parseInt(rs.getCell(8, rowCnt).getContents().trim());
			rowCnt += 4;

			/*
			 * ��ϵͳ�ܶ����ݱ�� Pipe.No ����I0 �յ��J0 ����LP ֱ��DP Ħ��ϵ�� ��˱�� �ն˱�� 1 0 1 28.5
			 * 0.3 0.017 3.894 3.842 2 1 2 32 0.3 0.017 3.842 3.784 3 2 3 28.6
			 * 0.3 0.017 3.784 3.733 4 3 4 25.4 0.3 0.017 3.733 3.687 5 4 5 24.7
			 * 0.3 0.017 3.687 3.643 6 5 6 23.5 0.3 0.017 3.643 3.601 7 6 7 30.4
			 * 0.3 0.017 3.601 3.546 8 8 7 15.5 0.3 0.017 3.731 3.171 9 9 6 4.3
			 * 0.3 0.017 3.886 3.7
			 */
			I0 = new int[NP];
			J0 = new int[NP];
			lp = new double[NP];
			dpl = new double[NP];
			slp = new double[NP];
			ZJup = new double[NP];
			ZJdw = new double[NP];
			for (int j = 0; j < NP; j++)
			{
				I0[j] = Integer.parseInt(rs.getCell(1, rowCnt + j).getContents().trim());
				J0[j] = Integer.parseInt(rs.getCell(2, rowCnt + j).getContents().trim());
				lp[j] = Double.parseDouble(rs.getCell(3, rowCnt + j).getContents().trim());
				dpl[j] = Double.parseDouble(rs.getCell(4, rowCnt + j).getContents().trim());
				slp[j] = Double.parseDouble(rs.getCell(5, rowCnt + j).getContents().trim());
				ZJup[j] = Double.parseDouble(rs.getCell(6, rowCnt + j).getContents().trim());
				ZJdw[j] = Double.parseDouble(rs.getCell(7, rowCnt + j).getContents().trim());
			}
			rowCnt += NP;
			rowCnt += 3;

			/*
			 * ��ϵͳ�ڵ����ݱ��ڵ�No ��ˮ���ha ����ϵ�� ������ ���ױ�� 1 3.5 0.6 5.244 ��δ�õ� 2 3.5
			 * 0.6 5.191 3 3.5 0.6 5.177 4 3.5 0.6 5.208 5 3.5 0.6 5.221 6 3.5
			 * 0.6 5.201 7 3.5 0.6 5.2 8 3.5 0.6 5.121 9 3.5 0.6 5.131 10 3.5
			 * 0.6 5.186
			 */
			Aj = new double[NN];
			Acoef = new double[NN];
			Hj = new double[NN];
			for (int j = 0; j < NN; j++)
			{
				Aj[j] = Double.parseDouble(rs.getCell(1, rowCnt + j).getContents().trim());
				Acoef[j] = Double.parseDouble(rs.getCell(2, rowCnt + j).getContents().trim());
				Hj[j] = Double.parseDouble(rs.getCell(3, rowCnt + j).getContents().trim());
			}
			rowCnt += NN;
			rowCnt += 3;

			// **************��Y3�汾��ȥ��**********
			/**
			 * ����·����&·���ڵ�Žڵ���� 1 2 3 4 5 6 7 8 1 0 1 2 3 4 5 6 7 2 8 7 -99 -99
			 * -99 -99 -99 -99 3 9 6 -99 -99 -99 -99 -99 -99
			 */
			/**
			 * Mroute = new int[Nstart][Nr_node]; for (int j = 0; j < Nstart;
			 * j++) { for (int k = 0; k < Nr_node; k++) { Mroute[j][k] =
			 * Integer.parseInt(rs.getCell(k + 1, rowCnt +
			 * j).getContents().trim()); } } rowCnt += Nstart; rowCnt += 3;
			 */
			// *******************************
			// ��������-·������·���ڵ�ž���
			Mroute = new int[Nstart][Nr_node];
			Mstart = new int[Nstart];

			// *************��һ�汾ȥ����ȥ��******
			/**
			 * for (int j = 0; j < Nstart; j++) { Mstart[j] =
			 * Integer.parseInt(rs.getCell(j + 1, rowCnt).getContents().trim());
			 * } rowCnt += 1; rowCnt += 3;
			 */
			// ************************************

			/*
			 * ��ϵͳ��֧·���ܶ����ݾ��� ����pipe branches-reverse order �ڵ���� 1 2 3 4 5 6 7 1
			 * 6 5 4 3 2 1 0 2 7 -99 -99 -99 -99 -99 -99 3 8 -99 -99 -99 -99 -99
			 * -99
			 */
			Mbranch = new int[Nstart][Npline];
			// *********��һ�汾��ȥ��***********
			/**
			 * for (int j = 0; j < Nstart; j++) { for (int k = 0; k < Npline;
			 * k++) { Mbranch[j][k] = Integer.parseInt(rs.getCell(k + 1, rowCnt
			 * + j).getContents().trim()); } }
			 */
			// ----�ٽ�ˮ��������----
			double Ad0, qkpmax, Hwdwkp, yykp, sita;

			// ----�м�ָ�����----
			int i00, j00 = 0, Ni1, jj, jp0, inp = 0, jpp, NPP;

			// �����ֲ�֧�߹ܶξ���-��������
			int[] Npjun = new int[NP];
			int[][] MNP = new int[NN][Ncol];

			// �м����
			int i, j, k, ik, jk, jjj, INS, it, k1, kp, in1, in2, in3, NR1, NR2, ii, Nprt, iprt1 = 0, iprt2 = 0;
			double H00, ycd0;
			double dtnt, taa, tbb, AA, XX1, XX2, hdj0;
			double[] XX = new double[NT];
			double[] qit = new double[NT];
			double[][] sumqj = new double[NT][NN];
			double[][] sumAj = new double[NT][NN];
			double[][] Tnode = new double[NN][NN];
			double[][] sumTnode = new double[NN][NN];
			double[] vp = new double[NP];
			double[] slop = new double[NP];
			double[][] qpt = new double[NT][NP];
			double[][] qqkp = new double[NT][NP];
			double[][] vpt = new double[NT][NP];
			double[][] rid = new double[NT][NP];
			double[][] slopt = new double[NT][NP];
			double[][] Hwup = new double[NT][NP];
			double[][] Hwdw = new double[NT][NP];
			double[][] hdcc0 = new double[NT][NP];
			double[][] overflow = new double[NT][NN];
			double[][] Hw_over = new double[NT][NN];
			double[][] Hwj = new double[NT][NN];

			// ----------------------------------------------------------------------------------------------------------
			String FileName = "";
			if (gjId != null)
			{
				FileName = gjId.substring(0, 12) + ".txt";
			}
			else
			{
				FileName = subSys + ".txt";
			}
			FileOutputStream fs = new FileOutputStream(new File(FilePath + FileName));
			PrintStream printStream = new PrintStream(fs);
			printStream.println(FileName);

			DecimalFormat df = new DecimalFormat("##.####");
			DecimalFormat df1 = new DecimalFormat("######.##");
			// --��������ļ���ʼ---
			// ================= ����ֵ ===============================
			//
			for (i = 0; i < NT; i++)
			{
				for (j = 0; j < NN; j++)
					sumAj[i][j] = 0;
			}
			for (i = 0; i < NT; i++)
			{
				for (j = 0; j < NN; j++)
					sumqj[i][j] = 0;
			}
			for (i = 0; i < NN; i++)
			{
				for (j = 0; j < NN; j++)
				{
					if (i == j)
					{
						Tnode[i][j] = 0;
					}
					else
					{
						Tnode[i][j] = -99;
					}
				}
			}
			for (i = 0; i < NN; i++)
			{
				for (j = 0; j < NN; j++)
					sumTnode[i][j] = 0;
			}
			// ====20161106===== ���ɾ��� MNP[i][j] ====
			for (i = 0; i < NN; i++)
			{
				for (j = 0; j < Ncol; j++)
				{
					MNP[i][j] = 0;
				}
				MNP[i][0] = i;
				jj = 2;
				for (k = 0; k < NP; k++)
				{
					if (J0[k] == i)
					{
						jj = jj + 1;
						MNP[i][1] = MNP[i][1] + 1;
						MNP[i][jj] = k;
					}
					if (I0[k] == i)
					{
						MNP[i][2] = MNP[i][2] + 1;
					}
				}
			}
			//System.out.println("===========  print MNP[i][j]");
			printStream.print("===========  print MNP[i][j]");
			for (i = 0; i < NN; i++)
			{
				for (j = 0; j < Ncol; j++)
				{
					printStream.printf("%6d", MNP[i][j]);
				}
				printStream.println();
			}
			// ----- MNP[i][j] ���� ------
			// ====20161112===== ���ɾ��� Mstart[i] ====
			jj = -1;
			for (i = 0; i < NN; i++)
			{
				if (MNP[i][1] == 0)
				{
					jj = jj + 1;
					Mstart[jj] = i;
				}
			}
			printStream.println("===========  print Mstart[i]");
			for (i = 0; i < Nstart; i++)
			{
				printStream.printf("%6d", Mstart[i]);
			}
			printStream.println();
			// ====20161029===== ���ɾ��� Mroute[i][j] ====
			for (i = 0; i < Nstart; i++)
			{
				for (j = 0; j < Nr_node; j++)
					Mroute[i][j] = -99;
			}
			for (i = 0; i < Nstart; i++)
				Mroute[i][0] = Mstart[i];
			for (i = 0; i < Nstart; i++)
			{
				for (j = 1; j < Nr_node; j++)
				{
					for (k = 0; k < NP; k++)
					{
						if (I0[k] == Mroute[i][j - 1])
						{
							Mroute[i][j] = J0[k];
						}
					}
				}
			}
			// ====20161106===== ���ɾ���Mbranch[i][j] ====
			for (i = 0; i < NP; i++)
			{
				Npjun[i] = 1;
			}
			for (i = 0; i < Nstart; i++)
			{
				for (j = 0; j < Npline; j++)
				{
					Mbranch[i][j] = -99;
				}
			}
			i00 = -1;
			NPP = 0;
			while (true)
			{
				if (NPP < NP)
				{
					for (i = 0; i < NN; i++)
					{
						if (MNP[i][2] == 0 && MNP[i][1] > 0)
						{
							jj = 2;
							Ni1 = MNP[i][1];
							for (j = 0; j < Ni1; j++)
							{
								jj = jj + 1;
								jp0 = MNP[i][jj];
								if (Npjun[jp0] > 0)
								{
									i00 = i00 + 1;
									j00 = 0;
									Mbranch[i00][j00] = jp0;
									inp = I0[jp0];
									Npjun[jp0] = -99;
									NPP = NPP + 1;
								}

								// L100:
								while (true)
								{
									INS = 1;
									for (jjj = 0; jjj < Nstart; jjj++)
									{
										if (Mstart[jjj] == inp)
										{
											INS = 0;
											break;
										}
									}
									if (INS > 0)
									{
										for (jpp = 0; jpp < NP; jpp++)
										{
											if (J0[jpp] == inp && Npjun[jpp] > 0)
											{
												j00 = j00 + 1;
												Mbranch[i00][j00] = jpp;
												inp = I0[jpp];
												Npjun[jpp] = -99;
												NPP = NPP + 1;
												// goto L100;
												break;
											}
											else
											{
												continue;
											}
										}
									}
									else
									// --- end of if(INS>0) ---
									{
										break;
									}
								}
							} // --- end of for(j=0;j<Ni1;j++) ---
						} // --- end of if(MNP[i][2]==0 && MNP[i][1]>0) ---
						MNP[i][2] = -99;
					}
					for (i = 0; i < NN; i++)
					{
						for (j = 0; j < NP; j++)
						{
							if (I0[j] == i && Npjun[j] < 0)
							{
								MNP[i][2] = 0;
							}
						}
					}// --- end of for(i=0;i<NN;1++) ---
				}
				else
				{
					break;
				}
			}
			// === ���ɾ��� Mbranch[i][j] ����====
			// ==================Tnode-sumTnode=========================
			for (i = 0; i < NP; i++)
			{
				vp[i] = vp0;
			}
			for (kp = 0; kp < NP; kp++)
			{
				in1 = I0[kp];
				in2 = J0[kp];
				Tnode[in1][in2] = lp[kp] / vp[kp] / 60;
				slop[kp] = (ZJup[kp] - ZJdw[kp]) / lp[kp];
			}
			//
			for (i = 0; i < Nstart; i++)
			{
				for (j = 0; j < Nr_node; j++)
				{
					in1 = Mroute[i][j];
					if (in1 >= 0)
					{
						for (k = j + 1; k < Nr_node; k++)
						{
							in2 = Mroute[i][k - 1];
							in3 = Mroute[i][k];
							if (in3 >= 0)
							{
								sumTnode[in1][in3] = sumTnode[in1][in2] + Tnode[in2][in3];
							}
						}
					}
				}
			}
			//System.out.println("pipe no.  I0    J0");
			for (i = 0; i < NP; i++)
			{
				//System.out.printf("%6d%6d%6d", i, I0[i], J0[i]);
				//System.out.println();
			}
			printStream.println();
			printStream.println("=====print pipe no.  I0    J0=====");
			printStream.print(" ip=");
			for (i = 0; i < NP; i++)
			{
				printStream.printf("%4d", i);
			}
			printStream.println();
			printStream.print(" I0=");
			for (i = 0; i < NP; i++)
			{
				printStream.printf("%4d", I0[i]);
			}
			printStream.println();
			printStream.print(" J0=");
			for (i = 0; i < NP; i++)
			{
				printStream.printf("%4d", J0[i]);
			}
			printStream.println();
			printStream.println();
			printStream.println("===========  print Mroute[i][j]");
			for (i = 0; i < Nstart; i++)
			{
				for (j = 0; j < Nr_node; j++)
				{
					printStream.printf("%6d", Mroute[i][j]);
				}
				printStream.println();
			}
			printStream.println();
			printStream.println("===========  print Mbranch[i][j]");
			for (i = 0; i < Nstart; i++)
			{
				for (j = 0; j < Npline; j++)
				{
					printStream.printf("%6d", Mbranch[i][j]);
				}
				printStream.println();
			}
			printStream.println("===========  print Tnode[i][j]");
			printStream.println("====j=  ");
			printStream.print("      ");
			for (j = 0; j < NN; j++)
			{
				printStream.printf("%6d", j);
			}
			printStream.println();
			for (i = 0; i < NN; i++)
			{
				if (i < 10)
				{
					printStream.print("i=" + i + "   ");
				}
				else
				{
					printStream.print("i=" + i + "  ");
				}

				for (j = 0; j < NN; j++)
				{
					if (Tnode[i][j] < 0.0)
					{
						printStream.print("      ");
					}
					else
					{
						printStream.printf("%6.2f", Tnode[i][j]);
					}
				}
				printStream.println();
			}
			printStream.println();
			printStream.println("===========  print sumTnode[i][j]");
			printStream.println("==j=  ");
			for (j = 0; j < NN; j++)
			{
				printStream.printf("%6d", j);
			}
			printStream.println();

			for (i = 0; i < NN; i++)
			{
				printStream.print("i=" + i + "   ");
				for (j = 0; j < NN; j++)
				{
					if (sumTnode[i][j] <= 0.0)
					{
						printStream.print("      ");
					}
					else
					{
						printStream.printf("%6.2f", sumTnode[i][j]);
					}
				}
				printStream.println();
			}
			// ================= ����׼��̬����ģ��============================
			//
			// -------------------��̬ģ����������-----------------------------
			// ----------------�ڵ��ˮ���(ha)�ͻ�ˮ����(m3/sec)����--------

			printStream.println();
			printStream.println("===========  ������̬ģ�����      �����ڣ� " + P_simu + "  ��   ʱ������ " + NT + "       �յ�ˮλ�� " + Hw_end + "  m  =========");
			// ֥�Ӹ������--rainfall intensity at every time step--
			AA = A1 + A1 * C_storm * Math.log(P_simu) / 2.303;
			for (it = 0; it < NT; it++)
			{
				if (it <= NR)
				{
					dtnt = dt * (float) (it);
					tbb = dt * (float) (NR) - dtnt;
					XX1 = AA * ((1.0 - n_storm) * tbb / rc + b_storm);
					XX2 = Math.pow((tbb / rc + b_storm), (n_storm + 1.0));
				}
				else
				{
					dtnt = dt * (float) (it);
					taa = dtnt - dt * (float) (NR);
					XX1 = AA * ((1.0 - n_storm) * taa / (1.0 - rc) + b_storm);
					XX2 = Math.pow((taa / (1.0 - rc) + b_storm), (n_storm + 1.0));
				}
				XX[it] = XX1 / XX2;
				qit[it] = 167.0 * XX[it] / 1000.0;
			}
			//
			NR1 = NR - 1;
			NR2 = NR + 1;
			qit[NR] = (qit[NR] + qit[NR - 1] + qit[NR + 1]) / 3.0;
			printStream.println();
			printStream.println("    it      dtnt      XX[it]     qit[it]");
			for (it = 0; it < NT; it++)
			{
				dtnt = dt * (float) (it);
				printStream.printf("%6d%10.2f%12.6f%12.6f", it, dtnt, XX[it], qit[it]);
				printStream.println();

			}
			printStream.println();
			// =====֥�Ӹ������--����=====
			// =====����ڵ㼯ˮ���sumAj[it][j]=====
			for (it = 0; it < NT; it++)
			{
				dtnt = dt + dt * (float) (it);
				for (j = 0; j < NN; j++)
				{
					sumAj[it][j] = Aj[j];
					sumqj[it][j] = Aj[j] * qit[it] * Acoef[j];
					for (i = 0; i < NN; i++)
					{
						if (sumTnode[i][j] > 0 && sumTnode[i][j] < dtnt)
						{
							sumAj[it][j] = sumAj[it][j] + Aj[i];
							sumqj[it][j] = sumqj[it][j] + Aj[i] * qit[it] * Acoef[i];
						}
					}
				}
			}
			// print sumAj[it][j] and sumqj[it][j]
			printStream.println("  sumAj[it][j]=");
			for (it = 0; it < NT; it++)
			{
				for (j = 0; j < NN; j++)
				{
					printStream.printf("%8.2f", sumAj[it][j]);
				}
				printStream.println();
			}
			printStream.println();
			printStream.println("  sumqj[it][j]=");
			for (it = 0; it < NT; it++)
			{
				for (j = 0; j < NN; j++)
				{
					printStream.printf("%8.2f", sumqj[it][j]);
				}
				printStream.println();
			}
			printStream.println();
			// -------------�ܶ�ˮ�����㿪ʼ--------------
			// ---------------------------------------------------------------
			for (it = 0; it < NT; it++)
			{
				for (i = 0; i < NN; i++)
				{
					overflow[it][i] = 0.0;
					Hw_over[it][i] = 0.0;
				}
			}
			for (it = 0; it < NT; it++)
			{
				for (j = 0; j < NP; j++)
				{
					qpt[it][j] = -99.0;
					qqkp[it][j] = 0.0;
				}
			}
			// ---------------------------------------------------------------
			for (it = 0; it < NT; it++)
			// --1--
			{
				printStream.print(" it=" + it + "  qpt[it][k]=");
				for (j = 0; j < NN; j++)
				{
					for (k = 0; k < NP; k++)
					{
						if (I0[k] == j)
						{
							qpt[it][k] = sumqj[it][j];
							// s.Format("%8.2lf",qpt[it][k]); outfile<<s;
							printStream.printf("%8.2f", qpt[it][k]);
						}
					}
				}
				printStream.println();
				// -------------------20090127-sql����------------------------
				for (ik = 0; ik < Nstart; ik++)
				// --2--
				{
					for (jk = 0; jk < Npline; jk++)
					// --3--
					{
						kp = Mbranch[ik][jk];
						if (kp >= 0)
						// --4--
						{
							if (J0[kp] == Nend)
							{
								Hwdw[it][kp] = Hw_end;
								if (Iprt == 1)
								{
									printStream.println("   it= " + it + "   kp= " + kp + "  Hwdm= " + Hwdw[it][kp] + "  Hw_end= " + Hw_end);
								}
							}
							else
							{
								for (k1 = 0; k1 < NP; k1++)
								{
									if (I0[k1] == J0[kp]) Hwdw[it][kp] = Hwup[it][k1];
								}
							}
							//
							Ad0 = 0.7854 * Math.pow(dpl[kp], 2.0);
							hdj0 = ZJdw[kp] + dpl[kp];
							if (Hwdw[it][kp] >= hdj0)
							{
								if (Iprt == 1)
								{
									printStream.println("   it= " + it + "   kp= " + kp + "  Hwdm= " + Hwdw[it][kp] + "  ��û���� ");
								}
								hdcc0[it][kp] = 1.0;
								rid[it][kp] = dpl[kp] / 4.0;
								vpt[it][kp] = qpt[it][kp] / Ad0;
								slopt[it][kp] = 10.29 * Math.pow(slp[kp], 2.0) * Math.pow(qpt[it][kp], 2.0) / Math.pow(dpl[kp], 5.333);
								Hwup[it][kp] = Hwdw[it][kp] + slopt[it][kp] * lp[kp];
								if (Hwup[it][kp] >= Hj[I0[kp]])
								{
									Hwup[it][kp] = Hj[I0[kp]];
									slopt[it][kp] = (Hwup[it][kp] - Hwdw[it][kp]) / lp[kp];
									if (slopt[it][kp] < 0.0)
									{
										slopt[it][kp] = Math.abs(slopt[it][kp]);
									}
									vpt[it][kp] = Math.pow(rid[it][kp], 0.6667) * Math.pow(slopt[it][kp], 0.5) / slp[kp];
									qqkp[it][kp] = vpt[it][kp] * Ad0;
									if (qqkp[it][kp] < 0.0)
									{
										qqkp[it][kp] = Math.abs(qqkp[it][kp]);
									}
								}
							}
							else
							// --5--
							{
								if (Iprt == 1)
								{
									printStream.println("   it= " + it + "   kp= " + kp + "  Hwdw= " + Hwdw[it][kp] + "  ����û���� ");
								}
								// --20161018�޸Ŀ�ʼ---�����ٽ�ˮ����㷨-----------------------
								//
								qkpmax = 2.699 * Math.pow(dpl[kp], 2.5);
								if (qpt[it][kp] > qkpmax)
								{
									if (Iprt == 1)
									{
										printStream.println("   it= " + it + "   kp= " + kp + "  qkpmax= " + qkpmax + "  ����û���ܳ��� ");
									}
									vpt[it][kp] = qpt[it][kp] / Ad0;
									// H00=pow(vpt[it][kp],2.0)/13.72;
									// Hwdw[it][kp]=ZJdw[kp]+dpl[kp]+H00;
									Hwdw[it][kp] = ZJdw[kp] + dpl[kp];
									hdcc0[it][kp] = 1.0;
									rid[it][kp] = dpl[kp] / 4.0;
									slopt[it][kp] = 10.29 * Math.pow(slp[kp], 2.0) * Math.pow(qpt[it][kp], 2.0) / Math.pow(dpl[kp], 5.333);
									Hwup[it][kp] = Hwdw[it][kp] + slopt[it][kp] * lp[kp];
									if (Hwup[it][kp] >= Hj[I0[kp]])
									{
										Hwup[it][kp] = Hj[I0[kp]];
										slopt[it][kp] = (Hwup[it][kp] - Hwdw[it][kp]) / lp[kp];
										if (slopt[it][kp] < 0.0)
										{
											slopt[it][kp] = Math.abs(slopt[it][kp]);
										}
										vpt[it][kp] = Math.pow(rid[it][kp], 0.6667) * Math.pow(slopt[it][kp], 0.5) / slp[kp];
										qqkp[it][kp] = vpt[it][kp] * Ad0;
										if (qqkp[it][kp] < 0.0)
										{
											qqkp[it][kp] = Math.abs(qqkp[it][kp]);
										}
									}
								}
								else
								{
									if (Iprt == 1)
									{
										printStream.println("   it= " + it + "   kp= " + kp + "  Hwdm= " + Hwdw[it][kp] + "  ����û�����ܳ��� ");
									}
									// ==20161115�޸�---���þ���������ˮ��򻯹�ʽ��ʼ--------
									ycd0 = 20.1538 * slp[kp] * qpt[it][kp] / Math.pow(dpl[kp], 2.6667) / Math.pow(slop[kp], 0.5);
									if (ycd0 <= 1.5)
									{
										hdcc0[it][kp] = 0.27 * Math.pow(ycd0, 0.485);
									}
									else
									{
										hdcc0[it][kp] = 0.098 * ycd0 + 0.19;
									}
									if (hdcc0[it][kp] > 1.0)
									{
										hdcc0[it][kp] = 1.0;
									}
									// ==20161115�޸�---���þ���������ˮ��򻯹�ʽ����--------
									sita = 2.0 * Math.acos(1.0 - 2.0 * hdcc0[it][kp]);
									rid[it][kp] = 0.25 * dpl[kp] * (sita - Math.sin(sita)) / sita;
									vpt[it][kp] = Math.pow(rid[it][kp], 0.6667) * Math.pow(slop[kp], 0.5) / slp[kp];
								}
								// ---if(qpt[it][kp]>qkpmax)����---
								Hwdwkp = ZJdw[kp] + hdcc0[it][kp] * dpl[kp];
								if (Hwdwkp >= Hwdw[it][kp])
								{
									Hwdw[it][kp] = Hwdwkp;
								}
								else
								{
									yykp = Hwdw[it][kp] - ZJdw[kp];
									if (yykp > dpl[kp])
									{
										yykp = dpl[kp];
									}
									sita = 2.0 * Math.acos(1.0 - 2.0 * yykp / dpl[kp]);
									hdcc0[it][kp] = yykp / dpl[kp];
									rid[it][kp] = 0.25 * dpl[kp] * (sita - Math.sin(sita)) / sita;
									vpt[it][kp] = Math.pow(rid[it][kp], 0.6667) * Math.pow(slop[kp], 0.5) / slp[kp];
								}
								Hwup[it][kp] = Hwdw[it][kp] + slop[kp] * lp[kp];
							} // 5--end
								// ------- ���it������ ----------
							if (Iprt == 1)
							{
								printStream.println("   it= " + it + "   kp= " + kp + "   I0[kp]= " + I0[kp] + "  Hwdm= " + Hwdw[it][kp] + "  Hwup= " + Hwup[it][kp] + "  Hj= " + Hj[I0[kp]] + "  hdcc0= " + hdcc0[it][kp] + "  qpt= " + qpt[it][kp] + "  qqkp= " + qqkp[it][kp] + "  vpt= " + vpt[it][kp]);
							}
						}// --4 if(kp>=0) end
					}// --3 ---jk end
				}// --2---ik end
				printStream.println();
				printStream.println("    it   �ܶκ�  I0   J0 �ܾ�dpl     �ܶ�qp ˮ���뾶R  ������ ����(m/s)  ����ˮλ  ����ˮλ  �Ϲܵ׸�  �¹ܵ׸�  �ܶ��¶�  �ϵ����");
				for (i = 0; i < NP; i++)
				{
					printStream.printf("%6d%6d%6d%5d%8.2f%12.3f%10.3f%8.3f%10.3f%10.3f%10.3f%10.3f%10.3f%10.5f%10.3f", it, i, I0[i], J0[i], dpl[i], qpt[it][i], rid[it][i], hdcc0[it][i], vpt[it][i], Hwup[it][i], Hwdw[it][i], ZJup[i], ZJdw[i], slop[i], Hj[I0[i]]);
					printStream.println();
				}
				printStream.println();
				// -------------- ��ʼ����ڵ�ˮλ-�ڵ��ˮ���ͻ�ˮ��� ---------------
				for (i = 0; i < NP; i++)
				{
					k = J0[i];
					if (k == Nend)
					{
						Hwj[it][k] = Hwdw[it][i];
					}
					{
						j = I0[i];
						Hwj[it][j] = Hwup[it][i];
						if (Hwup[it][i] == Hj[j])
						{
							overflow[it][j] = overflow[it - 1][j] + (qpt[it][i] - qqkp[it][i]) * dt * 60.0;
							Hw_over[it][j] = csf * overflow[it][j] / Aj[j] / 10000.0 * 1000.0;

						}
						if (Hwup[it][i] < Hj[j] && it > 0 && overflow[it - 1][j] > 0.0)
						{
							overflow[it][j] = overflow[it - 1][j] * 0.90;
							Hw_over[it][j] = csf * overflow[it][j] / Aj[j] / 10000.0 * 1000.0;
						}
					}
					if (it > NR && Hw_over[it][j] <= 5.0)
					{
						overflow[it][j] = 0.0;
						Hw_over[it][j] = 0.0;
					}

				}
				// ------------------ ���������ڵ���� ---------------
			}// 1-- it end ---
				// ----------------��Ļ����������------
			//System.out.println("------ ģ�ͼ���ȫ����� ------");
			// --------------------------------- ����ܶγ����ȼ����� ---------------
			// outfile<<" ======== ʱ�ιܶγ����� ========"<<endl;
			printStream.println(" ======== ʱ�ιܶγ����� ========");
			Nprt = NP / Nprtc + 1;
			for (ii = 0; ii < Nprt; ii++)
			{
				{
					iprt1 = ii * Nprtc;
					iprt2 = iprt1 + Nprtc;
					if (iprt2 > NP)
					{
						iprt2 = NP;
					}
				}
				printStream.print("  i=    ");
				for (i = iprt1; i < iprt2; i++)
				{
					if (i < 10)
					{
						printStream.print("    " + i + "   ");
					}
					else
					{
						printStream.print("   " + i + "   ");
					}
				}
				printStream.println();
			}
			printStream.println("it=");
			for (it = 0; it < NT; it++)
			{
				if (it < 10)
				{
					printStream.print(" " + it + "   ");
				}
				else
				{
					printStream.print(it + "   ");
				}
				for (i = iprt1; i < iprt2; i++)
				{
					printStream.printf("%8.3f", hdcc0[it][i]);
				}
				printStream.println();
			}
			// ----------- ����ڵ�ˮλ������ ---------------
			printStream.println(" ======== ʱ�νڵ�ˮλ ========");
			Nprt = NN / Nprtc + 1;
			for (ii = 0; ii < Nprt; ii++)
			{
				{
					iprt1 = ii * Nprtc;
					iprt2 = iprt1 + Nprtc;
					if (iprt2 > NN)
					{
						iprt2 = NN;
					}
				}
				printStream.print("  i=    ");
				for (i = iprt1; i < iprt2; i++)
				{
					if (i < 10)
					{
						printStream.print("    " + i + "   ");
					}
					else
					{
						printStream.print("   " + i + "   ");
					}
				}
				printStream.println();
				printStream.println("it=");
				for (it = 0; it < NT; it++)
				{
					if (it < 10)
					{
						printStream.print(" " + it + "   ");
					}
					else
					{
						printStream.print(it + "   ");
					}

					for (i = iprt1; i < iprt2; i++)
					{
						printStream.printf("%8.2f", Hwj[it][i]);
					}
					printStream.println();
				}
			}
			// ***********��֯���ݣ�����ҳ��������ʾ********
			for (it = 0; it < NT; it++)
			{
				String WaterLevNew = "";
				for (i = 0; i < NN; i++)
				{
					if (gjId != null && i == SubgjId)
					{
						WaterAccGj += df1.format(Hwj[it][i]) + "|";
					}
					WaterLevNew += df1.format(Hwj[it][i]) + "|";
				}
				WaterLev[it] = WaterLevNew;
			}
			// *************************************
			
			// --------------- ����ڵ����������� ---------------

			printStream.println(" ======== ʱ�νڵ��ˮ��(m3) ========");
			Nprt = NN / Nprtc + 1;
			for (ii = 0; ii < Nprt; ii++)
			{
				{
					iprt1 = ii * Nprtc;
					iprt2 = iprt1 + Nprtc;
					if (iprt2 > NN)
					{
						iprt2 = NN;
					}
				}
				printStream.print("  i=    ");
				for (i = iprt1; i < iprt2; i++)
				{
					if (i < 10)
					{
						printStream.print("    " + i + "   ");
					}
					else
					{
						printStream.print("   " + i + "   ");
					}
				}
				printStream.println();
				printStream.println("it=");
				for (it = 0; it < NT; it++)
				{
					if (it < 10)
					{
						printStream.print(" " + it + "   ");
					}
					else
					{
						printStream.print(it + "   ");
					}
					for (i = iprt1; i < iprt2; i++)
					{
						if (overflow[it][i] <= 0.0)
						{
							printStream.print("        ");
						}
						else
						{
							printStream.printf("%8.2f", overflow[it][i]);
						}
					}
					printStream.println();
				}
			}
			// ***********��֯���ݣ�����ҳ��������ʾ********
			for (it = 0; it < NT; it++)
			{
				String WaterAccNew = "";
				for (i = 0; i < NN; i++)
				{
					if (overflow[it][i] <= 0.0)
					{
						WaterAccNew += 0 + "|";
					}
					else
					{
						WaterAccNew += df1.format(overflow[it][i]) + "|";
					}
				}
				WaterAcc[it] = WaterAccNew;
			}
			// *********************************************
			printStream.println(" ======== ʱ�νڵ��ˮ���(mm) ========");
			Nprt = NN / Nprtc + 1;
			for (ii = 0; ii < Nprt; ii++)
			{
				{
					iprt1 = ii * Nprtc;
					iprt2 = iprt1 + Nprtc;
					if (iprt2 > NN)
					{
						iprt2 = NN;
					}
				}
				printStream.print("  i=    ");
				for (i = iprt1; i < iprt2; i++)
				{
					if (i < 10)
					{
						printStream.print("    " + i + "   ");
					}
					else
					{
						printStream.print("   " + i + "   ");
					}
				}
				printStream.println();
				printStream.println("it=");
				for (it = 0; it < NT; it++)
				{
					if (it < 10)
					{
						printStream.print(" " + it + "   ");
					}
					else
					{
						printStream.print(it + "   ");
					}
					for (i = iprt1; i < iprt2; i++)
					{
						if (overflow[it][i] <= 0.0)
						{
							printStream.print("        ");
						}
						else
						{
							printStream.printf("%8.2f", Hw_over[it][i]);
						}
					}
					printStream.println();

				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return gjName;
		}
		if (AnalogWaterType.equals("WaterAccGj"))
		{
			return WaterAccGj;
		}
		else if (AnalogWaterType.equals("WaterAcc"))
		{
			String WaterAccList = "";
			for (int i = 0; i < WaterAcc.length; i++)
			{
				WaterAccList += subSys.substring(7, 12) + WaterAcc[i] + ";";
			}
			return WaterAccList;
		}
		else if (AnalogWaterType.equals("WaterLev"))
		{
			String WaterLevList = "";
			for (int i = 0; i < WaterLev.length; i++)
			{
				WaterLevList += subSys.substring(7, 12) + WaterLev[i] + ";";
			}
			return WaterLevList;
		}
		return "";
	}

	// ģ�����۵�һ��
	private String analog_W1(String subSys, int timePeriod, String gjId, String AnalogWaterType)
	{
		int SubgjId = 0;
		if (gjId != null)
		{
			SubgjId = CommUtil.StrToInt(gjId.substring(12, 15)) - 1;
		}
		try
		{
			// subSys = 900001_WJ001
			// �����������ݣ�
			// �ܶ������ڵ������ܵ��������·�����ܶ�����ģ��ʱ����,
			// �ܵ�·������·�����ڵ������յ�ڵ�ţ��м�������ļ�ָ�룬������ݱ�����
			int NP = 9, NN = 10, Nstart = 3, Npline = 7, NT = 24, Nr_node = 8, Nend = 7, Iprt = 0, Nprtc = 20;
			// ��ˮ��������
			// �˾�����ˮ����m/d��, �ܶ��趨����vp0��ʱ�䲽����h������ϵͳ�յ�ˮλ�����氼͹ϵ��csf
			double q1 = 0.45, vp0 = 0.8, dt = 1.0, Hw_end = 4.1, csf = 3.0;
			// �ڵ�������(ha)�� �ڵ�����ߣ�m�����ڵ�����˿�(��)
			double[] Aj;
			double[] Hj;
			double[] Rj;
			// ��ˮ���仯���ߣ�NT��
			double[] Rf;
			// ����·������·���ڵ��(��99��ʾ�սڵ�)
			int[][] Mroute;
			int[][] Mbranch;
			// �ܶ����νڵ��I0,���νڵ��J0���ܶγ���(m),Ħ��ϵ��
			int[] I0;
			int[] J0;
			double[] lp;
			double[] slp;
			// �ڵ�����
			int[] Mstart;
			// �ܶ�ֱ��(m)�����ιܵ׸߳�(m)�����ιܵ׸߳�(m)
			double[] dpl;
			double[] ZJup;
			double[] ZJdw;

			this.FileSaveRoute = "/www/DPP-LOCAL/DPP-LOCAL-WEB/files/analogData/";
			String XlsPath = "";
			if (gjId != null)
			{
				XlsPath = FileSaveRoute + gjId.substring(0, 12) + ".xls";
				gjName = gjId.substring(0, 12);
			}
			else
			{
				XlsPath = FileSaveRoute + subSys + ".xls";
				gjName = subSys;
			}
			InputStream is = new FileInputStream(XlsPath);
			Workbook rwb = Workbook.getWorkbook(is);
			Sheet rs = rwb.getSheet(0);
			int rsRows = rs.getRows();

			/*
			 * �������ݱ����ϵͳ�� �ڵ���NN �ܶ���NP �����NStart ·���ܶ���Npline ·���ڵ���Nr_node
			 * �յ���ں�Nend ģ��ʱ��NT �ܶ�·����NrouteYJ002 10 9 3 7 8 8 60 3
			 */
			int rowCnt = 2;
			String sysName = rs.getCell(0, rowCnt).getContents().trim();
			NN = Integer.parseInt(rs.getCell(1, rowCnt).getContents().trim());
			NP = Integer.parseInt(rs.getCell(2, rowCnt).getContents().trim());
			Nstart = Integer.parseInt(rs.getCell(3, rowCnt).getContents().trim());
			Npline = Integer.parseInt(rs.getCell(4, rowCnt).getContents().trim());
			Nr_node = Integer.parseInt(rs.getCell(5, rowCnt).getContents().trim());
			Nend = Integer.parseInt(rs.getCell(6, rowCnt).getContents().trim());
			NT = Integer.parseInt(rs.getCell(7, rowCnt).getContents().trim());
			rowCnt += 4;

			/*
			 * ��ϵͳ�ܶ����ݱ�� Pipe.No ����I0 �յ��J0 ����LP ֱ��DP Ħ��ϵ�� ��˱�� �ն˱�� 1 0 1 28.5
			 * 0.3 0.017 3.894 3.842 2 1 2 32 0.3 0.017 3.842 3.784 3 2 3 28.6
			 * 0.3 0.017 3.784 3.733 4 3 4 25.4 0.3 0.017 3.733 3.687 5 4 5 24.7
			 * 0.3 0.017 3.687 3.643 6 5 6 23.5 0.3 0.017 3.643 3.601 7 6 7 30.4
			 * 0.3 0.017 3.601 3.546 8 8 7 15.5 0.3 0.017 3.731 3.171 9 9 6 4.3
			 * 0.3 0.017 3.886 3.7
			 */
			I0 = new int[NP];
			J0 = new int[NP];
			lp = new double[NP];
			dpl = new double[NP];
			slp = new double[NP];
			ZJup = new double[NP];
			ZJdw = new double[NP];
			for (int j = 0; j < NP; j++)
			{
				I0[j] = Integer.parseInt(rs.getCell(1, rowCnt + j).getContents().trim());
				J0[j] = Integer.parseInt(rs.getCell(2, rowCnt + j).getContents().trim());
				lp[j] = Double.parseDouble(rs.getCell(3, rowCnt + j).getContents().trim());
				dpl[j] = Double.parseDouble(rs.getCell(4, rowCnt + j).getContents().trim());
				slp[j] = Double.parseDouble(rs.getCell(5, rowCnt + j).getContents().trim());
				ZJup[j] = Double.parseDouble(rs.getCell(6, rowCnt + j).getContents().trim());
				ZJdw[j] = Double.parseDouble(rs.getCell(7, rowCnt + j).getContents().trim());
			}
			rowCnt += NP;
			rowCnt += 3;

			/*
			 * ��ϵͳ�ڵ����ݱ�� �ڵ�No �������Aj ������ �ڵ�����˿� 1 0.2 5.244 80 2 0.2 5.191 80 3
			 * 0.2 5.177 80 4 0.2 5.208 80 5 0.2 5.221 80 6 0.2 5.201 80 7 0.2
			 * 5.2 80 8 0.2 5.121 80 9 0.2 5.131 80 10 0.2 5.186 80
			 */
			Aj = new double[NN];
			Hj = new double[NN];
			Rj = new double[NN];
			for (int j = 0; j < NN; j++)
			{
				Aj[j] = Double.parseDouble(rs.getCell(1, rowCnt + j).getContents().trim());
				Hj[j] = Double.parseDouble(rs.getCell(2, rowCnt + j).getContents().trim());
				Rj[j] = Double.parseDouble(rs.getCell(3, rowCnt + j).getContents().trim());
			}
			rowCnt += NN;
			rowCnt += 3;

			Mroute = new int[Nstart][Nr_node];
			/**
			 * ����·������ ��� 1 2 3 ���� 0 8 9
			 */
			Mstart = new int[Nstart];
			for (int j = 0; j < Nstart; j++)
			{
				Mstart[j] = Integer.parseInt(rs.getCell(j + 1, rowCnt).getContents().trim());
			}
			rowCnt += 1;
			rowCnt += 3;

			/*
			 * ��ϵͳ��֧·���ܶ����ݾ��� ���� �ڵ���� 1 2 3 4 5 6 7 1 6 5 4 3 2 1 0 2 7 -99 -99
			 * -99 -99 -99 -99 3 8 -99 -99 -99 -99 -99 -99
			 */
			Mbranch = new int[Nstart][Npline];
			for (int j = 0; j < Nstart; j++)
			{
				for (int k = 0; k < Npline; k++)
				{
					Mbranch[j][k] = Integer.parseInt(rs.getCell(k + 1, rowCnt + j).getContents().trim());
				}
			}
			rowCnt += Nstart;
			rowCnt += 3;

			/*
			 * ��ˮ���仯���� ʱ�� 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21
			 * 22 23 24 ���� 2.12 2.19 2.18 2.8 3.21 3.9 5.2 5.62 5.63 5.08 5.12
			 * 5.69 5.28 4.52 4.51 4.58 5.5 5.62 5.13 5.18 3.4 3.12 2.22 2.2
			 */
			Rf = new double[NT];
			for (int j = 0; j < NT; j++)
			{
				Rf[j] = Double.parseDouble(rs.getCell(j + 1, rowCnt).getContents().trim());
			}
			// ----�м����----
			int i, j, k, ik, jk, it, k1, kp, in1, in2, in3, NR1, NR2, ii, Nprt, iprt1, iprt2;
			double Ad0, qkpmax, Hwdwkp, H00, ycd0, yykp, sita;
			double dtnt;
			double[] qit = new double[NT];
			double[][] sumqj = new double[NT][NN];
			double[][] sumRj = new double[NT][NN];
			double[][] Tnode = new double[NN][NN];
			double[][] sumTnode = new double[NN][NN];
			double sumqjj, hdj0;
			// taa,tbb,AA,XX1,XX2,XX[NT],
			double[] vp = new double[NP];
			double[] slop = new double[NP];
			double[][] qpt = new double[NT][NP];
			double[][] qqkp = new double[NT][NP];
			double[][] vpt = new double[NT][NP];
			double[][] rid = new double[NT][NP];
			double[][] slopt = new double[NT][NP];
			double[][] Hwup = new double[NT][NP];
			double[][] Hwdw = new double[NT][NP];
			double[][] hdcc0 = new double[NT][NP];
			double[][] overflow = new double[NT][NN];
			double[][] Hw_over = new double[NT][NN];
			double[][] Hwj = new double[NT][NN];

			DecimalFormat df = new DecimalFormat("##.####");
			DecimalFormat df1 = new DecimalFormat("######.##");
			
			String FileName = subSys + ".txt";
			FileOutputStream fs = new FileOutputStream(new File(FilePath + FileName));
			PrintStream printStream = new PrintStream(fs);
			printStream.println("20161030-��ˮ����ģ��-���ҳ�-3.txt");
			// System.out.println("------ ��ˮ����ģ��-���ҳ� ------");
			// ================= ����ֵ ===============================
			for (i = 0; i < NT; i++)
			{
				for (j = 0; j < NN; j++)
					sumRj[i][j] = 0;
			}
			for (i = 0; i < NT; i++)
			{
				for (j = 0; j < NN; j++)
					sumqj[i][j] = 0;
			}
			for (i = 0; i < NN; i++)
			{
				for (j = 0; j < NN; j++)
				{
					if (i == j)
					{
						Tnode[i][j] = 0;
					}
					else
					{
						Tnode[i][j] = -99;
					}
				}
			}
			for (i = 0; i < NN; i++)
			{
				for (j = 0; j < NN; j++)
					sumTnode[i][j] = 0;
			}
			// =====20161029===== ���ɾ��� Mroute[i][j] ====
			for (i = 0; i < Nstart; i++)
			{
				for (j = 0; j < Nr_node; j++)
					Mroute[i][j] = -99;
			}
			for (i = 0; i < Nstart; i++)
				Mroute[i][0] = Mstart[i];
			for (i = 0; i < Nstart; i++)
			{
				for (j = 1; j < Nr_node; j++)
				{
					for (k = 0; k < NP; k++)
					{
						if (I0[k] == Mroute[i][j - 1])
						{
							Mroute[i][j] = J0[k];
						}
					}
				}
			}
			// ==================Tnode-sumTnode=========================
			for (i = 0; i < NP; i++)
				vp[i] = vp0;
			for (kp = 0; kp < NP; kp++)
			{
				in1 = I0[kp];
				in2 = J0[kp];
				Tnode[in1][in2] = lp[kp] / vp[kp] / 3600;
				slop[kp] = (ZJup[kp] - ZJdw[kp]) / lp[kp];
			}
			//
			for (i = 0; i < Nstart; i++)
			{
				for (j = 0; j < Nr_node; j++)
				{
					in1 = Mroute[i][j];
					if (in1 >= 0)
					{
						for (k = j + 1; k < Nr_node; k++)
						{
							in2 = Mroute[i][k - 1];
							in3 = Mroute[i][k];
							if (in3 >= 0)
							{
								sumTnode[in1][in3] = sumTnode[in1][in2] + Tnode[in2][in3];
							}
						}
					}
				}
			}
			printStream.println("pipe no.  I0    J0");
			for (i = 0; i < NP; i++)
			{
				// System.out.printf("%6d%6d%6d", i, I0[i], J0[i]);
				// System.out.println();
			}
			printStream.println();
			printStream.print(" ip=");
			for (i = 0; i < NP; i++)
			{
				printStream.printf("%4d", i);
			}
			printStream.println();
			printStream.print(" I0=");
			for (i = 0; i < NP; i++)
			{
				printStream.printf("%4d", I0[i]);
			}
			printStream.println();
			printStream.print(" J0=");
			for (i = 0; i < NP; i++)
			{
				printStream.printf("%4d", J0[i]);
			}
			printStream.println();
			printStream.println();
			printStream.println("===========  print Mroute[i][j]");
			for (i = 0; i < Nstart; i++)
			{
				for (j = 0; j < Nr_node; j++)
				{
					printStream.printf("%6d", Mroute[i][j]);
				}
				printStream.println();
			}
			printStream.println();
			printStream.println("===========  print Mbranch[i][j]");
			for (i = 0; i < Nstart; i++)
			{
				for (j = 0; j < Npline; j++)
				{
					printStream.printf("%6d", Mbranch[i][j]);
				}
				printStream.println();
			}
			printStream.println("===========  print Tnode[i][j]");
			printStream.println("====j=  ");
			printStream.print("      ");
			for (j = 0; j < NN; j++)
			{
				printStream.printf("%6d", j);
			}
			printStream.println();
			for (i = 0; i < NN; i++)
			{
				if (i < 10)
				{
					printStream.print("i=" + i + "   ");
				}
				else
				{
					printStream.print("i=" + i + "  ");
				}
				for (j = 0; j < NN; j++)
				{
					if (Tnode[i][j] < 0.0)
					{
						printStream.print("      ");
					}
					else
					{
						printStream.printf("%6.2f", Tnode[i][j]);
					}
				}
				printStream.println();
			}
			printStream.println();
			printStream.println("===========  print sumTnode[i][j]");
			printStream.print("==j=  ");
			for (j = 0; j < NN; j++)
			{
				printStream.printf("%6d", j);
			}
			printStream.println();
			for (i = 0; i < NN; i++)
			{
				printStream.print("i=" + i + "   ");
				for (j = 0; j < NN; j++)
				{
					if (sumTnode[i][j] <= 0.0)
					{
						printStream.print("      ");
					}
					else
					{
						printStream.printf("%6.2f", sumTnode[i][j]);
					}
				}
				printStream.println();
			}

			// ----------------���ܶ��ܷ����˿�(��)�ͻ�ˮ����(m3/sec)����------
			printStream.println();
			printStream.println("======  ��ˮ������̬ģ��   �˾�����ˮ���� " + q1 + "  m3   ʱ������ " + NT + "       �յ�ˮλ�� " + Hw_end + "  m  =====");

			// �˾���ˮ���仯����---discharge at every time step per head---
			for (it = 0; it < NT; it++)
			{
				qit[it] = q1 * Rf[it] / 100.0 / 3600;
			}
			printStream.println();
			printStream.println("    it     qit[it]");
			for (it = 0; it < NT; it++)
			{
				printStream.printf("%6d%12.6f", it, qit[it]);
				printStream.println();
			}
			printStream.println();
			for (it = 0; it < NT; it++)
			{
				dtnt = dt + dt * (float) (it);
				for (j = 0; j < NN; j++)
				{
					sumRj[it][j] = Rj[j];
					sumqj[it][j] = Rj[j] * qit[it];
					for (i = 0; i < NN; i++)
					{
						if (sumTnode[i][j] > 0 && sumTnode[i][j] < dtnt)
						{
							sumRj[it][j] = sumRj[it][j] + Rj[i];
							sumqj[it][j] = sumqj[it][j] + Rj[i] * qit[it];
						}
					}
				}
			}
			printStream.println("  sumRj[it][j]=");
			for (it = 0; it < NT; it++)
			{
				for (j = 0; j < NN; j++)
				{
					printStream.printf("%8.2f", sumRj[it][j]);
				}
				printStream.println();
			}
			printStream.println();
			printStream.println("  sumqj[it][j] x 1000 =");
			for (it = 0; it < NT; it++)
			{
				for (j = 0; j < NN; j++)
				{
					sumqjj = sumqj[it][j] * 1000.0;
					printStream.printf("%8.2f", sumqjj);
				}
				printStream.println();
			}
			printStream.println();
			for (it = 0; it < NT; it++)
			{
				for (i = 0; i < NN; i++)
				{
					overflow[it][i] = 0.0;
					Hw_over[it][i] = 0.0;
				}
			}
			for (it = 0; it < NT; it++)
			{
				for (j = 0; j < NP; j++)
				{
					qpt[it][j] = -99.0;
					qqkp[it][j] = 0.0;
				}
			}
			for (it = 0; it < NT; it++)
			{
				printStream.print(" it=" + it + "  qpt[it][k]=");
				for (j = 0; j < NN; j++)
				{
					for (k = 0; k < NP; k++)
					{
						if (I0[k] == j)
						{
							qpt[it][k] = sumqj[it][j];
							printStream.printf("%8.2f", qpt[it][k]);
						}
					}
				}
				printStream.println();
				for (ik = 0; ik < Nstart; ik++)
				{
					for (jk = 0; jk < Npline; jk++)
					{
						kp = Mbranch[ik][jk];
						if (kp >= 0)
						{
							if (J0[kp] == Nend)
							{
								Hwdw[it][kp] = Hw_end;
								if (Iprt == 1)
								{
									printStream.println("   it= " + it + "   kp= " + kp + "  Hwdm= " + Hwdw[it][kp] + "  Hw_end= " + Hw_end);
								}
							}
							else
							{
								for (k1 = 0; k1 < NP; k1++)
								{
									if (I0[k1] == J0[kp]) Hwdw[it][kp] = Hwup[it][k1];
								}
							}
							Ad0 = 0.7854 * Math.pow(dpl[kp], 2.0);
							hdj0 = ZJdw[kp] + dpl[kp];
							if (Hwdw[it][kp] >= hdj0)
							{
								if (Iprt == 1)
								{
									printStream.println("   it= " + it + "   kp= " + kp + "  Hwdm= " + Hwdw[it][kp] + "  ��û���� ");
								}
								hdcc0[it][kp] = 1.0;
								rid[it][kp] = dpl[kp] / 4.0;
								vpt[it][kp] = qpt[it][kp] / Ad0;
								slopt[it][kp] = 10.29 * Math.pow(slp[kp], 2.0) * Math.pow(qpt[it][kp], 2.0) / Math.pow(dpl[kp], 5.333);
								Hwup[it][kp] = Hwdw[it][kp] + slopt[it][kp] * lp[kp];
								if (Hwup[it][kp] >= Hj[I0[kp]])
								{
									Hwup[it][kp] = Hj[I0[kp]];
									slopt[it][kp] = (Hwup[it][kp] - Hwdw[it][kp]) / lp[kp];
									if (slopt[it][kp] < 0.0)
									{
										slopt[it][kp] = Math.abs(slopt[it][kp]);
									}
									vpt[it][kp] = Math.pow(rid[it][kp], 0.6667) * Math.pow(slopt[it][kp], 0.5) / slp[kp];
									qqkp[it][kp] = vpt[it][kp] * Ad0;
									if (qqkp[it][kp] < 0.0)
									{
										qqkp[it][kp] = Math.abs(qqkp[it][kp]);
									}
								}
							}
							else
							{
								if (Iprt == 1)
								{
									printStream.println("   it= " + it + "   kp= " + kp + "  Hwdw= " + Hwdw[it][kp] + "  ����û���� ");
								}
								qkpmax = 2.699 * Math.pow(dpl[kp], 2.5);
								if (qpt[it][kp] > qkpmax)
								{
									if (Iprt == 1)
									{
										printStream.println("   it= " + it + "   kp= " + kp + "  qkpmax= " + qkpmax + "  ����û���ܳ��� ");
									}
									vpt[it][kp] = qpt[it][kp] / Ad0;
									// H00=pow(vpt[it][kp],2.0)/13.72;
									// Hwdw[it][kp]=ZJdw[kp]+dpl[kp]+H00;
									Hwdw[it][kp] = ZJdw[kp] + dpl[kp];
									hdcc0[it][kp] = 1.0;
									rid[it][kp] = dpl[kp] / 4.0;
									slopt[it][kp] = 10.29 * Math.pow(slp[kp], 2.0) * Math.pow(qpt[it][kp], 2.0) / Math.pow(dpl[kp], 5.333);
									Hwup[it][kp] = Hwdw[it][kp] + slopt[it][kp] * lp[kp];
									if (Hwup[it][kp] >= Hj[I0[kp]])
									{
										Hwup[it][kp] = Hj[I0[kp]];
										slopt[it][kp] = (Hwup[it][kp] - Hwdw[it][kp]) / lp[kp];
										if (slopt[it][kp] < 0.0)
										{
											slopt[it][kp] = Math.abs(slopt[it][kp]);
										}
										vpt[it][kp] = Math.pow(rid[it][kp], 0.6667) * Math.pow(slopt[it][kp], 0.5) / slp[kp];
										qqkp[it][kp] = vpt[it][kp] * Ad0;
										if (qqkp[it][kp] < 0.0)
										{
											qqkp[it][kp] = Math.abs(qqkp[it][kp]);
										}
									}
								}
								else
								{
									if (Iprt == 1)
									{
										printStream.println("   it= " + it + "   kp= " + kp + "  Hwdm= " + Hwdw[it][kp] + "  ����û�����ܳ��� ");
									}
									// ==20161018�޸Ŀ�ʼ---�����ٽ�ˮ��򻯹�ʽ--------zhou-p21------
									ycd0 = qpt[it][kp] / 2.983 / Math.pow(dpl[kp], 2.5);
									hdcc0[it][kp] = Math.pow(ycd0, 0.513);
									sita = 2.0 * Math.acos(1.0 - 2.0 * hdcc0[it][kp]);
									rid[it][kp] = 0.25 * dpl[kp] * (sita - Math.sin(sita)) / sita;
									vpt[it][kp] = Math.pow(rid[it][kp], 0.6667) * Math.pow(slop[kp], 0.5) / slp[kp];
								}
								Hwdwkp = ZJdw[kp] + hdcc0[it][kp] * dpl[kp];
								if (Hwdwkp >= Hwdw[it][kp])
								{
									Hwdw[it][kp] = Hwdwkp;
								}
								else
								{
									yykp = Hwdw[it][kp] - ZJdw[kp];
									if (yykp > dpl[kp])
									{
										yykp = dpl[kp];
									}
									sita = 2.0 * Math.acos(1.0 - 2.0 * yykp / dpl[kp]);
									hdcc0[it][kp] = yykp / dpl[kp];
									rid[it][kp] = 0.25 * dpl[kp] * (sita - Math.sin(sita)) / sita;
									vpt[it][kp] = Math.pow(rid[it][kp], 0.6667) * Math.pow(slop[kp], 0.5) / slp[kp];
								}
								Hwup[it][kp] = Hwdw[it][kp] + slop[kp] * lp[kp];
							}
							if (Iprt == 1)
							{
								printStream.println("   it= " + it + "   kp= " + kp + "   I0[kp]= " + I0[kp] + "  Hwdm= " + Hwdw[it][kp] + "  Hwup= " + Hwup[it][kp] + "  Hj= " + Hj[I0[kp]] + "  hdcc0= " + hdcc0[it][kp] + "  qpt= " + qpt[it][kp] + "  qqkp= " + qqkp[it][kp] + "  vpt= " + vpt[it][kp]);
							}
						}
					}
				}
				printStream.println();
				printStream.println("    it   �ܶκ�  I0   J0 �ܾ�dpl     �ܶ�qp ˮ���뾶R  ������ ����(m/s)  ����ˮλ  ����ˮλ  �Ϲܵ׸�  �¹ܵ׸�  �ܶ��¶�  �ϵ����");
				for (i = 0; i < NP; i++)
				{
					printStream.printf("%6d%6d%6d%5d%8.2f%12.3f%10.3f%8.3f%10.3f%10.3f%10.3f%10.3f%10.3f%10.5f%10.3f", it, i, I0[i], J0[i], dpl[i], qpt[it][i], rid[it][i], hdcc0[it][i], vpt[it][i], Hwup[it][i], Hwdw[it][i], ZJup[i], ZJdw[i], slop[i], Hj[I0[i]]);
					printStream.println();
				}
				printStream.println();
				for (i = 0; i < NP; i++)
				{
					k = J0[i];
					if (k == Nend)
					{
						Hwj[it][k] = Hwdw[it][i];
					}
					{
						j = I0[i];
						Hwj[it][j] = Hwup[it][i];
						if (Hwup[it][i] == Hj[j])
						{
							overflow[it][j] = overflow[it - 1][j] + (qpt[it][i] - qqkp[it][i]) * dt * 60.0;
							Hw_over[it][j] = csf * overflow[it][j] / Aj[j] / 10000.0 * 1000.0;

						}
						if (Hwup[it][i] < Hj[j] && overflow[it][j] > 0.0)
						{
							overflow[it][j] = overflow[it - 1][j] * 0.90;
							Hw_over[it][j] = csf * overflow[it][j] / Aj[j] / 10000.0 * 1000.0;
						}
					}
					if (Hw_over[it][j] <= 5.0)
					{
						overflow[it][j] = 0.0;
						Hw_over[it][j] = 0.0;
					}
				}
			}
			// System.out.println("------ ģ�ͼ���ȫ����� ------");
			// --------------- ����ܶγ����ȼ����� ---------------
			printStream.println(" ======== ʱ�ιܶγ����� ========");
			Nprt = NP / Nprtc + 1;
			for (ii = 0; ii < Nprt; ii++)
			{
				{
					iprt1 = ii * Nprtc;
					iprt2 = iprt1 + Nprtc;
					if (iprt2 > NP)
					{
						iprt2 = NP;
					}
				}
				printStream.print("  i=    ");
				for (i = iprt1; i < iprt2; i++)
				{
					if (i < 10)
					{
						printStream.print("    " + i + "   ");
					}
					else
					{
						printStream.print("   " + i + "   ");
					}
				}
				printStream.println();
				printStream.println("it=");
				for (it = 0; it < NT; it++)
				{
					if (it < 10)
					{
						printStream.print(" " + it + "   ");
					}
					else
					{
						printStream.print(it + "   ");
					}
					for (i = iprt1; i < iprt2; i++)
					{
						printStream.printf("%8.3f", hdcc0[it][i]);
					}
					printStream.println();
				}
			}
			// --------------------- ����ڵ�ˮλ������ ---------------
			printStream.println(" ======== ʱ�νڵ�ˮλ ========");
			Nprt = NN / Nprtc + 1;
			for (ii = 0; ii < Nprt; ii++)
			{
				{
					iprt1 = ii * Nprtc;
					iprt2 = iprt1 + Nprtc;
					if (iprt2 > NN)
					{
						iprt2 = NN;
					}
				}
				printStream.print("  i=    ");
				for (i = iprt1; i < iprt2; i++)
				{
					if (i < 10)
					{
						printStream.print("    " + i + "   ");
					}
					else
					{
						printStream.print("   " + i + "   ");
					}
				}
				printStream.println();
				printStream.println("it=");
				for (it = 0; it < NT; it++)
				{
					if (it < 10)
					{
						printStream.print(" " + it + "   ");
					}
					else
					{
						printStream.print(it + "   ");
					}
					for (i = iprt1; i < iprt2; i++)
					{
						printStream.printf("%8.2f", Hwj[it][i]);
					}
					printStream.println();
				}
			}
			// ***********��֯���ݣ�����ҳ��������ʾ********
			for (it = 0; it < NT; it++)
			{
				String SewageLevNew = "";
				for (i = 0; i < NN; i++)
				{
					if (gjId != null && i == SubgjId)
					{
						SewageAccGj += df1.format(Hwj[it][i]) + "|";
					}
					SewageLevNew += df1.format(Hwj[it][i]) + "|";
				}
				SewageLev[it] = SewageLevNew;
			}
			// *************************************
			// ---------------- ����ڵ����������� ---------------
			printStream.println(" ======== ʱ�νڵ��ˮ��(m3) ========");
			Nprt = NN / Nprtc + 1;
			for (ii = 0; ii < Nprt; ii++)
			{
				{
					iprt1 = ii * Nprtc;
					iprt2 = iprt1 + Nprtc;
					if (iprt2 > NN)
					{
						iprt2 = NN;
					}
				}
				printStream.print("  i=    ");
				for (i = iprt1; i < iprt2; i++)
				{
					if (i < 10)
					{
						printStream.print("    " + i + "   ");
					}
					else
					{
						printStream.print("   " + i + "   ");
					}
				}
				printStream.println();
				printStream.println("it=");
				for (it = 0; it < NT; it++)
				{
					if (it < 10)
					{
						printStream.print(" " + it + "   ");
					}
					else
					{
						printStream.print(it + "   ");
					}

					for (i = iprt1; i < iprt2; i++)
					{
						if (overflow[it][i] <= 0.0)
						{
							printStream.print("        ");
						}
						else
						{
							printStream.printf("%8.2f", overflow[it][i]);
						}
					}
					printStream.println();
				}
			}
			for (it = 0; it < NT; it++)
			{
				String SewageAccNew = "";
				for (i = 0; i < NN; i++)
				{
					if (overflow[it][i] <= 0.0)
					{
						printStream.print("        ");
						SewageAccNew += 0 + "|";
					}
					else
					{
						printStream.printf("%8.2f", overflow[it][i]);
						SewageAccNew += df1.format(overflow[it][i]) + "|";
					}
				}
				SewageAcc[it] = SewageAccNew;
			}
			printStream.println(" ======== ʱ�νڵ��ˮ���(mm) ========");
			Nprt = NN / Nprtc + 1;
			for (ii = 0; ii < Nprt; ii++)
			{
				{
					iprt1 = ii * Nprtc;
					iprt2 = iprt1 + Nprtc;
					if (iprt2 > NN)
					{
						iprt2 = NN;
					}
				}
				printStream.print("  i=    ");
				for (i = iprt1; i < iprt2; i++)
				{
					if (i < 10)
					{
						printStream.print("    " + i + "   ");
					}
					else
					{
						printStream.print("   " + i + "   ");
					}
				}
				printStream.println();
				printStream.println("it=");
				for (it = 0; it < NT; it++)
				{
					if (it < 10)
					{
						printStream.println(" " + it + "   ");
					}
					else
					{
						printStream.println(it + "   ");
					}
					for (i = iprt1; i < iprt2; i++)
					{
						if (overflow[it][i] <= 0.0)
						{
							printStream.print("        ");
						}
						else
						{
							printStream.printf("%8.2f", Hw_over[it][i]);
						}
					}
					printStream.println();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return gjName;
		}
		if (AnalogWaterType.equals("SewageAccGj"))
		{
			return SewageAccGj;
		}
		else if (AnalogWaterType.equals("SewageAcc"))
		{
			String SewageAccList = "";
			for (int i = 0; i < SewageAcc.length; i++)
			{
				SewageAccList += subSys.substring(7, 12) + SewageAcc[i] + ";";
			}
			return SewageAccList;
		}
		else if (AnalogWaterType.equals("SewageLev"))
		{
			String SewageLevList = "";
			for (int i = 0; i < SewageLev.length; i++)
			{
				SewageLevList += subSys.substring(7, 12) + SewageLev[i] + ";";
			}
			return SewageLevList;
		}
		return "";
	}

	private String		FileSaveRoute = "/www/DPP-LOCAL-BETA/DPP-LOCAL-WEB/files/analogData/";
	private String      FilePath = "/www/DPP-LOCAL-BETA/DPP-LOCAL-WEB/files/analogValue/";
	private String		File_Name;
	private String		Sid;

	private String		gjName;

	private String		AnalogWaterType;
	private String[]	WaterAcc	= new String[60];
	private String[]	WaterLev	= new String[60];
	private String		WaterAccGj	= "";

	private String[]	SewageAcc	= new String[24];
	private String[]	SewageLev	= new String[24];
	private String		SewageAccGj	= "";
}
