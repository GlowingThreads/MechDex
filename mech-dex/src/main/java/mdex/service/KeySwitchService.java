package mdex.service;

import mdex.model.KeySwitch;

import java.util.List;
import java.util.Optional;

public interface KeySwitchService {

    KeySwitch createKeySwitch(KeySwitch keySwitch);

    Optional<KeySwitch> getKeySwitchById(String id);

    List<KeySwitch> getAllKeySwitchs();

    KeySwitch updateKeySwitch(KeySwitch keySwitch);

    void deleteKeySwitchById(String id);
}