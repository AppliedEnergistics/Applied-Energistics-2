package appeng.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.FakePlayer;

@MockitoSettings
class PlatformFakePlayerTest {

    @Test
    void ae2FakePlayerIsRecognized() {
        var fakePlayer = mock(FakePlayer.class);
        when(fakePlayer.getGameProfile()).thenReturn(new GameProfile(UUID.randomUUID(), "[AE2]"));

        assertThat(Platform.isAe2FakePlayer(fakePlayer)).isTrue();
    }

    @Test
    void fakePlayersFromOtherModsAreNotRecognized() {
        var otherFakePlayer = mock(FakePlayer.class);
        when(otherFakePlayer.getGameProfile()).thenReturn(new GameProfile(UUID.randomUUID(), "SomeOtherMod"));

        assertThat(Platform.isAe2FakePlayer(otherFakePlayer)).isFalse();
    }

    @Test
    void realPlayersAreNotRecognizedEvenIfSomehowNamedLikeAe2() {
        // Not stubbing getGameProfile(): rejection must happen purely by type, without inspecting the name.
        var realPlayer = mock(Player.class);

        assertThat(Platform.isAe2FakePlayer(realPlayer)).isFalse();
    }

    @Test
    void nullIsNotRecognized() {
        assertThat(Platform.isAe2FakePlayer(null)).isFalse();
    }
}
