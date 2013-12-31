package demo.vm;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.exporter.Interceptor;
import org.zkoss.exporter.RowRenderer;
import org.zkoss.exporter.excel.ExcelExporter;
import org.zkoss.exporter.excel.ExcelExporter.ExportContext;
import org.zkoss.exporter.pdf.FontFactory;
import org.zkoss.exporter.pdf.PdfExporter;
import org.zkoss.exporter.pdf.PdfPCellFactory;
import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.poi.ss.usermodel.CellStyle;
import org.zkoss.poi.ss.usermodel.Row;
import org.zkoss.poi.xssf.usermodel.XSSFCellStyle;
import org.zkoss.poi.xssf.usermodel.XSSFSheet;
import org.zkoss.poi.xssf.usermodel.XSSFWorkbook;
import org.zkoss.util.media.AMedia;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.TreeNode;

import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import demo.model.Food;
import demo.model.PackageData;
import demo.model.PackageDataUtil;

public class BinaryPackageVM {
	
	private TreeModel<TreeNode<PackageData>> _model;
	
	@Init
	public void init() {
		_model = new DefaultTreeModel<PackageData>(PackageDataUtil.getRoot()); 
	}
	
    public TreeModel<TreeNode<PackageData>> getTreeModel() {
        return _model;
    }
    
    @Command
    public void exportTreeToPdf(@BindingParam("ref") Tree tree) throws Exception {
    	PdfExporter exporter = new PdfExporter();
    	
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	exporter.export(tree, out);
    	
    	AMedia amedia = new AMedia("FirstReport.pdf", "pdf", "application/pdf", out.toByteArray());
    	Filedownload.save(amedia);
    	
    	out.close();
    }
    
    private List<TreeNode<PackageData>> getTreeData(TreeNode<PackageData> root) {
    	List<TreeNode<PackageData>> data = new ArrayList<TreeNode<PackageData>>();
    	
    	List<TreeNode<PackageData>> children = root.getChildren();
    	for (TreeNode<PackageData> child : children) {
   			data.add(child);
    		if (!child.isLeaf())
    			data.addAll(getTreeData(child));
    	}
    	
    	return data;
    }
    
    private int getLevel(TreeNode<?> node) {
    	TreeNode<?> parent = node.getParent();
    	if (parent == null)
    		return -1;
    	else
    		return getLevel(parent) + 1;
    }
    
    @Command
    public void exportTreeToPdfByDataModel() throws Exception {
		final PdfExporter exporter = new PdfExporter();
		final PdfPCellFactory cellFactory = exporter.getPdfPCellFactory();
		final FontFactory fontFactory = exporter.getFontFactory();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		final String[] headers = new String[]{"Path", "Description", "Size" };
		final String[] footers = new String[]{"footer1", "footer2", "footer3" };
		
		exporter.setInterceptor(new Interceptor <PdfPTable> () {
			@Override
			public void beforeRendering(PdfPTable table) {
				for (int i = 0; i < headers.length; i++) {
					String header = headers[i];
					Font font = exporter.getFontFactory().getFont(FontFactory.FONT_TYPE_HEADER);
					
					PdfPCell cell = exporter.getPdfPCellFactory().getHeaderCell();
					cell.setPhrase(new Phrase(header, font));
					if ("Size".equals(header)) {
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					}
					table.addCell(cell);
				}
				table.completeRow();
			}
			
			@Override
			public void afterRendering(PdfPTable table) {
				for (String footer : footers) {
					Font font = exporter.getFontFactory().getFont(FontFactory.FONT_TYPE_FOOTER);
					
					PdfPCell cell = exporter.getPdfPCellFactory().getFooterCell();
					cell.setPhrase(new Phrase(footer, font));
					table.addCell(cell);
				}
				table.completeRow();
			}
		});
		
		exporter.export(headers.length, getTreeData(_model.getRoot()), new RowRenderer<PdfPTable, TreeNode<PackageData>>() {
			
			private String indent(int level) {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < level; i++)
					sb.append("  ");
				return sb.toString();
			}

			@Override
			public void render(PdfPTable table, TreeNode<PackageData> node, boolean isOddRow) {
				Font font = fontFactory.getFont(FontFactory.FONT_TYPE_CELL);
				
				PackageData pkg = node.getData();
				
				PdfPCell cell = cellFactory.getCell(isOddRow);
				cell.setPhrase(new Phrase(indent(getLevel(node)) + pkg.getPath(), font));
				table.addCell(cell);
				
				cell = cellFactory.getCell(isOddRow);
				cell.setPhrase(new Phrase(pkg.getDescription(), font));
				table.addCell(cell);
				
				cell = cellFactory.getCell(isOddRow);
				cell.setPhrase(new Phrase(pkg.getSize(), font));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);
				
				table.completeRow();
			}

		}, out);
		
		AMedia amedia = new AMedia("FirstReport.pdf", "pdf", "application/pdf", out.toByteArray());
		Filedownload.save(amedia);
		
		out.close();    	
    }
    
    @Command
    public void exportTreeToExcel(@BindingParam("ref") Tree tree) throws Exception {
		ExcelExporter exporter = new ExcelExporter();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		exporter.export(tree, out);
		
		AMedia amedia = new AMedia("FirstReport.xlsx", "xls", "application/file", out.toByteArray());
		Filedownload.save(amedia);
		
		out.close();
    }
    
    @Command
    public void exportTreeToExcelByDataModel() throws Exception {
		final ExcelExporter exporter = new ExcelExporter();

		final String[] headers = new String[]{"Path", "Description", "Size"};
		final String[] footers = new String[]{"footer 1", "footer 2", "footer 3"};
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		exporter.setInterceptor(new Interceptor<XSSFWorkbook>() {
			
			@Override
			public void beforeRendering(XSSFWorkbook target) {
				ExportContext ctx = exporter.getExportContext();
				
				for (String header : headers) {
					Cell cell = exporter.getOrCreateCell(ctx.moveToNextCell(), ctx.getSheet());
					cell.setCellValue(header);
					
					if ("Size".equals(header)) {
						CellStyle srcStyle = cell.getCellStyle();
						if (srcStyle.getAlignment() != CellStyle.ALIGN_CENTER) {
							XSSFCellStyle newCellStyle = ctx.getSheet().getWorkbook().createCellStyle();
							newCellStyle.cloneStyleFrom(srcStyle);
							newCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
							cell.setCellStyle(newCellStyle);
						}
					}
				}
			}
			
			@Override
			public void afterRendering(XSSFWorkbook target) {
				ExportContext ctx = exporter.getExportContext();

				ctx.moveToNextRow();
				for (String footer : footers) {
					Cell cell = exporter.getOrCreateCell(ctx.moveToNextCell(), ctx.getSheet());
					cell.setCellValue(footer);
				}
			}
		});
		
		exporter.export(headers.length, getTreeData(_model.getRoot()), new RowRenderer<Row, TreeNode<PackageData>>() {

			private String indent(int level) {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < level; i++)
					sb.append("  ");
				return sb.toString();
			}
			
			@Override
			public void render(Row row, TreeNode<PackageData> node, boolean oddRow) {
				ExportContext ctx = exporter.getExportContext();
				XSSFSheet sheet = ctx.getSheet();
				
				PackageData pkg = node.getData();
				
				exporter
				.getOrCreateCell(ctx.moveToNextCell(), sheet)
				.setCellValue(indent(getLevel(node)) + pkg.getPath());
				
				exporter
				.getOrCreateCell(ctx.moveToNextCell(), sheet)
				.setCellValue(pkg.getDescription());
				
				Cell cell = exporter.getOrCreateCell(ctx.moveToNextCell(), sheet);
				cell.setCellValue(pkg.getSize());
				CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
				cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
				cell.setCellStyle(cellStyle);
			}

		}, out);
		
		AMedia amedia = new AMedia("FirstReport.xlsx", "xls", "application/file", out.toByteArray());
		Filedownload.save(amedia);
		
		out.close();    	
    }
    
}
