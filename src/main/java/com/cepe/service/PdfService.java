package com.cepe.service;

import com.cepe.model.Eleve;
import com.cepe.model.Matiere;
import com.cepe.model.Note;
import com.cepe.service.dto.ResultatEleve;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service de génération du Relevé de Notes PDF pour un élève.
 */
public class PdfService {

    private final NoteService noteService;
    private final EleveService eleveService;
    private final MatiereService matiereService;

    public PdfService() {
        this.noteService = new NoteServiceImpl();
        this.eleveService = new EleveServiceImpl();
        this.matiereService = new MatiereServiceImpl();
    }

    /**
     * Génère le relevé de notes PDF d'un élève pour une année scolaire donnée.
     *
     * @param numEleve      identifiant de l'élève
     * @param anneeScolaire année scolaire (ex: 2025-2026)
     * @param cheminSortie  chemin absolu du fichier PDF à créer
     * @throws Exception en cas d'erreur de génération
     */
    public void genererRelevePDF(int numEleve, String anneeScolaire, String cheminSortie) throws Exception {
        Eleve eleve = eleveService.findById(numEleve)
                .orElseThrow(() -> new IllegalArgumentException("Élève non trouvé"));

        List<Note> notes = noteService.findByEleveAndAnnee(numEleve, anneeScolaire);
        List<Matiere> matieres = matiereService.findAll();

        // Récupération du résultat avec rangs
        List<ResultatEleve> tousResultats = noteService.calculerResultatsGlobaux(anneeScolaire);
        ResultatEleve resultat = tousResultats.stream()
                .filter(r -> r.getEleve() != null && r.getEleve().getNumEleve() == numEleve)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Résultat de délibération non trouvé"));

        Map<Integer, Matiere> matiereMap = matieres.stream()
                .collect(Collectors.toMap(Matiere::getNumMat, m -> m));

        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(cheminSortie));
        document.open();

        // Polices avec java.awt.Color
        Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
        Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
        Font fontGras = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
        Font fontMention = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.DARK_GRAY);

        // Titre
        Paragraph titre = new Paragraph("RELEVÉ DE NOTES - SESSION CEPE", fontTitre);
        titre.setAlignment(Element.ALIGN_CENTER);
        document.add(titre);

        Paragraph annee = new Paragraph("Année scolaire : " + anneeScolaire, fontNormal);
        annee.setAlignment(Element.ALIGN_CENTER);
        annee.setSpacingAfter(20);
        document.add(annee);

        // Informations de l'élève
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(20);
        infoTable.setHorizontalAlignment(Element.ALIGN_LEFT);
        infoTable.setWidths(new float[]{30f, 70f});

        addInfoRow(infoTable, "Nom :", eleve.getNom(), fontGras, fontNormal);
        addInfoRow(infoTable, "Prénom(s) :", eleve.getPrenom(), fontGras, fontNormal);
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String dateNaiss = (eleve.getDateNaissance() != null) 
        ? eleve.getDateNaissance().format(formatter) 
        : "N/A";
        addInfoRow(infoTable, "Date de naissance :", dateNaiss, fontGras, fontNormal);
        String ecoleDesign = (resultat.getEleve() != null && resultat.getEleve().getEcole() != null) 
        ? resultat.getEleve().getEcole().getDesign() 
        : "N/A";
        addInfoRow(infoTable, "École d'origine :", ecoleDesign, fontGras, fontNormal);

        document.add(infoTable);

        // Tableau des notes
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        table.setWidths(new float[]{40f, 15f, 22.5f, 22.5f});

        // En-têtes
        String[] headers = {"Matière", "Coefficient", "Note (/20)", "Note Pondérée"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fontGras));
            cell.setBackgroundColor(new Color(226, 232, 240));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(8);
            table.addCell(cell);
        }

        BigDecimal totalPoints = BigDecimal.ZERO;
        int totalCoef = 0;

        for (Note note : notes) {
            Matiere matiere = matiereMap.get(note.getNumMat());
            if (matiere == null) continue;

            BigDecimal notePonderee = note.getNote().multiply(BigDecimal.valueOf(matiere.getCoef()));
            totalPoints = totalPoints.add(notePonderee);
            totalCoef += matiere.getCoef();

            PdfPCell cellMat = new PdfPCell(new Phrase(matiere.getDesignMat(), fontNormal));
            cellMat.setPadding(6);
            table.addCell(cellMat);

            PdfPCell cellCoef = new PdfPCell(new Phrase(String.valueOf(matiere.getCoef()), fontNormal));
            cellCoef.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellCoef.setPadding(6);
            table.addCell(cellCoef);

            PdfPCell cellNote = new PdfPCell(new Phrase(note.getNote().toString(), fontNormal));
            cellNote.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellNote.setPadding(6);
            table.addCell(cellNote);

            PdfPCell cellPond = new PdfPCell(new Phrase(notePonderee.toString(), fontNormal));
            cellPond.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellPond.setPadding(6);
            table.addCell(cellPond);
        }

        // Ligne Total
        PdfPCell cellTotalLabel = new PdfPCell(new Phrase("TOTAL", fontGras));
        cellTotalLabel.setBackgroundColor(new Color(248, 250, 252));
        cellTotalLabel.setPadding(6);
        table.addCell(cellTotalLabel);

        PdfPCell cellTotalCoef = new PdfPCell(new Phrase(String.valueOf(totalCoef), fontGras));
        cellTotalCoef.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellTotalCoef.setBackgroundColor(new Color(248, 250, 252));
        cellTotalCoef.setPadding(6);
        table.addCell(cellTotalCoef);

        PdfPCell cellVide = new PdfPCell(new Phrase("", fontGras));
        cellVide.setBackgroundColor(new Color(248, 250, 252));
        cellVide.setPadding(6);
        table.addCell(cellVide);

        PdfPCell cellTotalPoints = new PdfPCell(new Phrase(totalPoints.toString(), fontGras));
        cellTotalPoints.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellTotalPoints.setBackgroundColor(new Color(248, 250, 252));
        cellTotalPoints.setPadding(6);
        table.addCell(cellTotalPoints);

        // Ligne Moyenne Générale
        PdfPCell cellMoyLabel = new PdfPCell(new Phrase("MOYENNE GÉNÉRALE", fontGras));
        cellMoyLabel.setColspan(3);
        cellMoyLabel.setBackgroundColor(new Color(226, 232, 240));
        cellMoyLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellMoyLabel.setPadding(6);
        table.addCell(cellMoyLabel);

        BigDecimal moyenne = BigDecimal.ZERO;
        if (totalCoef > 0) {
            moyenne = totalPoints.divide(BigDecimal.valueOf(totalCoef), 2, RoundingMode.HALF_UP);
        }
        PdfPCell cellMoyVal = new PdfPCell(new Phrase(moyenne.toString(), fontGras));
        cellMoyVal.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellMoyVal.setBackgroundColor(new Color(226, 232, 240));
        cellMoyVal.setPadding(6);
        table.addCell(cellMoyVal);

        document.add(table);

        // Mention finale (Statut + Rang)
        Color couleurStatut = "ADMIS".equals(resultat.getStatut())
                ? new Color(22, 101, 52)
                : new Color(153, 27, 27);
        Font fontMentionColoree = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, couleurStatut);

        Paragraph mention = new Paragraph();
        mention.add(new Chunk("Statut : ", fontMention));
        mention.add(new Chunk(resultat.getStatut(), fontMentionColoree));
        mention.add(new Chunk("    |    Rang global : " + resultat.getRangGlobal()
                + "    |    Rang école : " + resultat.getRangEcole(), fontMention));
        mention.setAlignment(Element.ALIGN_CENTER);
        mention.setSpacingBefore(20);
        document.add(mention);

        // Pied de page
        Paragraph pied = new Paragraph(
                "Document généré par l'application Gestion Session CEPE",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY));
        pied.setAlignment(Element.ALIGN_CENTER);
        pied.setSpacingBefore(30);
        document.add(pied);

        document.close();
    }

    private void addInfoRow(PdfPTable table, String label, String value, Font fontLabel, Font fontValue) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, fontLabel));
        cellLabel.setBorder(Rectangle.NO_BORDER);
        cellLabel.setPadding(4);
        table.addCell(cellLabel);

        PdfPCell cellValue = new PdfPCell(new Phrase(value, fontValue));
        cellValue.setBorder(Rectangle.NO_BORDER);
        cellValue.setPadding(4);
        table.addCell(cellValue);
    }
}