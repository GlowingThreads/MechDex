package mdex.service;

import mdex.model.KeySwitch;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import net.datafaker.Faker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.random.RandomGenerator;

@Named("memoryKeySwitchService")
@ApplicationScoped
public class MemoryKeySwitchService implements KeySwitchService {

    private final List<KeySwitch> keySwitchs = new ArrayList<>();

    @PostConstruct
    public void init() {

        var faker = new Faker();
        var randomGenerator = RandomGenerator.getDefault();
        for (int counter = 1; counter <= 5; counter++) {
            var currentKeySwitch = new KeySwitch();
            currentKeySwitch.setId(UUID.randomUUID().toString());
            currentKeySwitch.setSwitchName(faker.mood().tone()+' '+faker.animal().name());
            currentKeySwitch.setSwitchType("Linear Switch");
            currentKeySwitch.setCompany(faker.darkSouls().covenants());
            currentKeySwitch.setActuationForce("55ug");
            currentKeySwitch.setSwitchTravel("2.2mm");
            keySwitchs.add(currentKeySwitch);
        }

    }

    @Override
    public KeySwitch createKeySwitch(KeySwitch keySwitch) {
        keySwitchs.add(keySwitch);
        return keySwitch;
    }

    @Override
    public Optional<KeySwitch> getKeySwitchById(String id) {
        return keySwitchs
                .stream()
                .filter(currentKeySwitch -> currentKeySwitch.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<KeySwitch> getAllKeySwitchs() {
        return keySwitchs;
    }

    @Override
    public KeySwitch updateKeySwitch(KeySwitch keySwitch) {
        return keySwitch;
    }

    @Override
    public void deleteKeySwitchById(String id) {
        Optional<KeySwitch> optionalKeySwitch = getKeySwitchById(id);
        if (optionalKeySwitch.isPresent()) {
            KeySwitch keySwitch = optionalKeySwitch.orElseThrow();
            keySwitchs.remove(keySwitch);
        } else {
            throw new RuntimeException("Could not find KeySwitch with id: " + id);
        }
    }
}