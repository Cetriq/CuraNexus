package se.curanexus.triage.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import se.curanexus.triage.domain.TriageProtocol;
import se.curanexus.triage.repository.TriageProtocolRepository;

import java.util.List;

@Configuration
@Profile("test")
public class TestDataInitializer {

    @Bean
    CommandLineRunner initTestData(TriageProtocolRepository protocolRepository) {
        return args -> {
            if (protocolRepository.count() == 0) {
                seedProtocols(protocolRepository);
            }
        };
    }

    private void seedProtocols(TriageProtocolRepository repository) {
        var chestPain = new TriageProtocol("RETTS-CHEST", "Chest Pain Protocol");
        chestPain.setDescription("Assessment protocol for patients presenting with chest pain. Based on RETTS guidelines.");
        chestPain.setCategory("Cardiovascular");
        chestPain.setVersion("1.0");
        chestPain.setActive(true);
        chestPain.addRedFlag("ST elevation on ECG");
        chestPain.addRedFlag("Hypotension (SBP < 90 mmHg)");
        chestPain.addRedFlag("Signs of heart failure");
        chestPain.addRedFlag("Altered consciousness");
        chestPain.addRedFlag("Radiating pain to jaw or arm");

        var respiratory = new TriageProtocol("RETTS-RESP", "Respiratory Distress Protocol");
        respiratory.setDescription("Assessment protocol for patients with breathing difficulties.");
        respiratory.setCategory("Respiratory");
        respiratory.setVersion("1.0");
        respiratory.setActive(true);
        respiratory.addRedFlag("SpO2 < 90% on room air");
        respiratory.addRedFlag("Unable to speak in full sentences");
        respiratory.addRedFlag("Cyanosis");
        respiratory.addRedFlag("Altered mental status");
        respiratory.addRedFlag("Silent chest on auscultation");

        var abdominal = new TriageProtocol("RETTS-ABD", "Abdominal Pain Protocol");
        abdominal.setDescription("Assessment protocol for patients presenting with abdominal pain.");
        abdominal.setCategory("Gastrointestinal");
        abdominal.setVersion("1.0");
        abdominal.setActive(true);
        abdominal.addRedFlag("Rigid abdomen");
        abdominal.addRedFlag("Signs of shock (hypotension, tachycardia)");
        abdominal.addRedFlag("Pulsatile abdominal mass (AAA)");
        abdominal.addRedFlag("Positive pregnancy test with pain/bleeding");
        abdominal.addRedFlag("Hematemesis or melena");

        var trauma = new TriageProtocol("RETTS-TRAUMA", "Trauma Assessment Protocol");
        trauma.setDescription("Primary and secondary survey for trauma patients.");
        trauma.setCategory("Trauma");
        trauma.setVersion("1.0");
        trauma.setActive(true);
        trauma.addRedFlag("GCS < 13");
        trauma.addRedFlag("Penetrating injury to head, neck, torso");
        trauma.addRedFlag("Flail chest");
        trauma.addRedFlag("Pelvic instability");
        trauma.addRedFlag("Amputation above wrist/ankle");

        var neuro = new TriageProtocol("RETTS-NEURO", "Neurological Emergency Protocol");
        neuro.setDescription("Assessment protocol for stroke, seizures, and altered consciousness.");
        neuro.setCategory("Neurological");
        neuro.setVersion("1.0");
        neuro.setActive(true);
        neuro.addRedFlag("Sudden onset severe headache");
        neuro.addRedFlag("Focal neurological deficit");
        neuro.addRedFlag("Seizure > 5 minutes");
        neuro.addRedFlag("Signs of raised intracranial pressure");
        neuro.addRedFlag("Symptoms within thrombolysis window (< 4.5 hours)");

        repository.saveAll(List.of(chestPain, respiratory, abdominal, trauma, neuro));
    }
}
