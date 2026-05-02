package will.dev.qrcodeApp.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import will.dev.qrcodeApp.entity.UserAction;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Date;

@Service
public class PdfGeneratorService {

    public byte[] generateUserActionsPdf(List<UserAction> userActions) throws DocumentException{
        // Utilisation d'un bloc try-catch-finally pour garantir la fermeture des ressources
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);

            document.open();

            // Titre
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Rapport des Actions Utilisateurs", fontHeader);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Date
            Font dateFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Paragraph date = new Paragraph("Généré le : " + sdf.format(new Date()), dateFont);
            date.setAlignment(Element.ALIGN_CENTER);
            document.add(date);

            document.add(new Paragraph("\n"));

            // Infos
            Font infoFont = new Font(Font.FontFamily.HELVETICA, 11);
            Paragraph info = new Paragraph("Nombre total d'actions : " + userActions.size(), infoFont);
            document.add(info);

            document.add(new Paragraph("\n"));

            // Table
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            addTableHeader(table);

            for (UserAction action : userActions) {
                table.addCell(safe(action.getId()));
                table.addCell(action.getUtilisateur() != null ? safe(action.getUtilisateur().getId()) : "N/A");
                table.addCell(action.getQrCode() != null ? safe(action.getQrCode().getId()) : "N/A");
                table.addCell(safe(action.getUniquePdfId()));
                table.addCell(safe(action.getIsRelatedToQrCode()));
                table.addCell(safe(action.getTypeAction()));
                table.addCell(safe(action.getDescription()));
                table.addCell(action.getDateAction() != null
                        ? action.getDateAction().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        : "N/A");
            }

            document.add(table);

            // 1. On ferme le document d'abord (écrit le trailer PDF)
            document.close();

            // 2. On s'assure que le writer a fini de vider son buffer vers le flux de sortie
            writer.close();

            // 3. On récupère les octets
            byte[] result = out.toByteArray();

            // DEBUG : Vérifier si le tableau d'octets commence bien par le header PDF "%PDF-"
            if (result.length > 0 && result[0] == 0x25 && result[1] == 0x50 && result[2] == 0x44 && result[3] == 0x46) {
                System.out.println("✅ PDF généré avec succès (Header valide)");
            } else {
                System.err.println("⚠️ Attention : Le PDF généré semble invalide ou vide");
            }

            return result;

        } catch (Exception e) {
            if (document.isOpen()) {
                document.close();
            }
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    private String safe(Object value) {
        return value != null ? String.valueOf(value) : "N/A";
    }

    private void addTableHeader(PdfPTable table) {
        String[] headers = {"ID", "Utilisateur ID", "QR Code ID", "PDF ID", "Lié au QR", "Type Action", "Description", "Date Action"};
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }
}
