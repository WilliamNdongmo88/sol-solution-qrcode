package will.dev.qrcodeApp.config;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import will.dev.qrcodeApp.entity.UserAction;
import will.dev.qrcodeApp.repository.UserActionRepository;
import will.dev.qrcodeApp.service.BrevoService;
import will.dev.qrcodeApp.service.PdfGeneratorService;
import com.itextpdf.text.DocumentException;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableBatchProcessing
@EnableScheduling
public class BatchConfig {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobRegistry jobRegistry;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private UserActionRepository userActionRepository;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Autowired
    private BrevoService brevoService;

    @Value("${brevo.recipient.email}")
    private String recipientEmail;

    // Configuration du lecteur (ItemReader)
    @Bean
    public RepositoryItemReader<UserAction> userActionReader() {
        Map<String, Sort.Direction> sorts = new HashMap<>();
        sorts.put("id", Sort.Direction.ASC);

        return new RepositoryItemReaderBuilder<UserAction>()
                .name("userActionReader")
                .repository(userActionRepository)
                .methodName("findAll")
                .pageSize(100) // Taille de page pour la lecture
                .sorts(sorts)
                .build();
    }

    // Configuration du processeur (ItemProcessor) - ici, il ne fait rien, juste passe les données
    @Bean
    public ItemProcessor<UserAction, UserAction> userActionProcessor() {
        return item -> item;
    }

    // Configuration de l\"écrivain (ItemWriter)
    @Bean
    public ItemWriter<UserAction> userActionWriter() {
        return items -> {
            // Générer le PDF avec toutes les actions lues
            try {
                byte[] pdfBytes = pdfGeneratorService.generateUserActionsPdf((List<UserAction>) items.getItems());
                //Files.write(Paths.get("debugs.pdf"), pdfBytes);
                String subject = "Rapport d\"actions utilisateurs du " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String body = "Veuillez trouver ci-joint le rapport quotidien des actions utilisateurs.";
                String attachmentName = "user_actions_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";

                // TODO: Remplacer \"recipientEmail\" par l\"adresse e-mail réelle du destinataire
                brevoService.sendEmailWithAttachment(recipientEmail, subject, body, pdfBytes, attachmentName);

                // Vider la table après l\"envoi du PDF
                userActionRepository.deleteAll();
                System.out.println("✅ Table user_actions vidée avec succès.");

            } catch (DocumentException e) {
                System.err.println("Erreur lors de la génération du PDF : " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Erreur lors de l\"envoi de l\"e-mail ou de la suppression des données : " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

    // Définition de l\"étape du Job
    @Bean
    public Step processUserActionsStep(ItemReader<UserAction> userActionReader, ItemProcessor<UserAction, UserAction> userActionProcessor, ItemWriter<UserAction> userActionWriter) {
        return new StepBuilder("processUserActionsStep", jobRepository)
                .<UserAction, UserAction>chunk(10, transactionManager) // Traite les éléments par lots de 10
                .reader(userActionReader)
                .processor(userActionProcessor)
                .writer(userActionWriter)
                .build();
    }

    // Définition du Job Spring Batch
    @Bean
    public Job userActionsJob(Step processUserActionsStep) {
        return new JobBuilder("userActionsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(processUserActionsStep)
                .end()
                .build();
    }

    // Scheduler pour déclencher le Job chaque nuit à 23h30
    //@Scheduled(cron = "0 30 23 * * *")
    @Scheduled(cron = "0 25 14 * * *")
    public void runUserActionsJob() {
        try {
            System.out.println("🚀 Déclenchement du Job userActionsJob à " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            //JobExecution execution = jobLauncher.run(userActionsJob, jobParameters);
            Job job = jobRegistry.getJob("userActionsJob");
            JobExecution execution = jobLauncher.run(job, jobParameters);
            System.out.println("✅ Job userActionsJob terminé avec le statut : " + execution.getStatus());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du déclenchement ou de l\"exécution du Job userActionsJob : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
