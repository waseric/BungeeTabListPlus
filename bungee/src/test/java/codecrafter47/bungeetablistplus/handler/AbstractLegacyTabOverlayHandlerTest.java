/*
 *     Copyright (C) 2020 Florian Stober
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.handler;

import codecrafter47.bungeetablistplus.protocol.PacketListenerResult;
import de.codecrafter47.taboverlay.handler.ContentOperationMode;
import de.codecrafter47.taboverlay.handler.RectangularTabOverlay;
import de.codecrafter47.taboverlay.handler.SimpleTabOverlay;
import net.md_5.bungee.api.score.Team;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class AbstractLegacyTabOverlayHandlerTest {
    private ClientTabList clientTabList;
    private MockLegacyTabList tabListHandler;

    @Before
    public void setUp() throws Exception {
        clientTabList = new ClientTabList();
        tabListHandler = new MockLegacyTabList(60, clientTabList);
    }

    @After
    public void tearDown() throws Exception {
        tabListHandler = null;
        clientTabList = null;
    }

    @Test
    public void testTabListSimple() {
        SimpleTabOverlay tabOverlay = tabListHandler.enterContentOperationMode(ContentOperationMode.SIMPLE.SIMPLE);

        tabOverlay.setSize(60);
        for (int i = 0; i < 60; i++) {
            tabOverlay.setText(i, String.format("Slot %02d", i));
            tabOverlay.setPing(i, i);
        }

        assertEquals(60, clientTabList.players.size());
        for (int i = 0; i < 60; i++) {
            assertEquals(String.format("Slot %02d", i), clientTabList.teams.get(clientTabList.playerToTeamMap.get(clientTabList.players.get(i))).getPrefix());
            assertEquals((Object) i, clientTabList.ping.get(clientTabList.players.get(i)));
        }

        tabOverlay.setSize(30);

        assertEquals(30, clientTabList.players.size());
        for (int i = 0; i < 30; i++) {
            assertEquals(String.format("Slot %02d", i), clientTabList.teams.get(clientTabList.playerToTeamMap.get(clientTabList.players.get(i))).getPrefix());
            assertEquals((Object) i, clientTabList.ping.get(clientTabList.players.get(i)));
        }

        tabListHandler.enterContentOperationMode(ContentOperationMode.PASS_TROUGH);

        assertEquals(0, clientTabList.players.size());
    }

    @Test
    public void testTabListBatch() {
        SimpleTabOverlay tabOverlay = tabListHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);

        tabOverlay.beginBatchModification();
        tabOverlay.setSize(60);
        for (int i = 0; i < 60; i++) {
            tabOverlay.setText(i, String.format("Slot %02d", i));
            tabOverlay.setPing(i, i);
        }
        tabOverlay.completeBatchModification();

        assertEquals(60, clientTabList.players.size());
        for (int i = 0; i < 60; i++) {
            assertEquals(String.format("Slot %02d", i), clientTabList.teams.get(clientTabList.playerToTeamMap.get(clientTabList.players.get(i))).getPrefix());
            assertEquals((Object) i, clientTabList.ping.get(clientTabList.players.get(i)));
        }
    }

    @Test
    public void testTabListPassThrough() {
        tabListHandler.enterContentOperationMode(ContentOperationMode.PASS_TROUGH);

        assertEquals(0, clientTabList.players.size());

        PlayerListItem packet = new PlayerListItem();
        PlayerListItem.Item item = new PlayerListItem.Item();
        packet.setItems(new PlayerListItem.Item[]{item});

        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        item.setDisplayName(new net.md_5.bungee.api.chat.TextComponent("Player"));
        item.setPing(15);

        tabListHandler.onPlayerListPacket(packet);

        assertEquals(1, clientTabList.players.size());
        assertEquals("Player", clientTabList.players.get(0));
        assertEquals((Object) 15, clientTabList.ping.get("Player"));

        item.setDisplayName(new net.md_5.bungee.api.chat.TextComponent("PlayerB"));

        tabListHandler.onPlayerListPacket(packet);

        assertEquals(2, clientTabList.players.size());
        assertEquals("Player", clientTabList.players.get(0));
        assertEquals((Object) 15, clientTabList.ping.get("Player"));
        assertEquals("PlayerB", clientTabList.players.get(1));
        assertEquals((Object) 15, clientTabList.ping.get("PlayerB"));

        tabListHandler.enterContentOperationMode(ContentOperationMode.RECTANGULAR);

        assertEquals(0, clientTabList.players.size());

        tabListHandler.enterContentOperationMode(ContentOperationMode.PASS_TROUGH);

        assertEquals(2, clientTabList.players.size());
        assertEquals("Player", clientTabList.players.get(0));
        assertEquals((Object) 15, clientTabList.ping.get("Player"));
        assertEquals("PlayerB", clientTabList.players.get(1));
        assertEquals((Object) 15, clientTabList.ping.get("PlayerB"));

        tabListHandler.onServerSwitch(false);

        assertEquals(0, clientTabList.players.size());

        tabListHandler.enterContentOperationMode(ContentOperationMode.RECTANGULAR);

        assertEquals(0, clientTabList.players.size());

        tabListHandler.onPlayerListPacket(packet);

        assertEquals(0, clientTabList.players.size());

        tabListHandler.enterContentOperationMode(ContentOperationMode.PASS_TROUGH);

        assertEquals(1, clientTabList.players.size());
        assertEquals("PlayerB", clientTabList.players.get(0));
        assertEquals((Object) 15, clientTabList.ping.get("PlayerB"));

        packet.setAction(PlayerListItem.Action.REMOVE_PLAYER);
        tabListHandler.onPlayerListPacket(packet);

        assertEquals(0, clientTabList.players.size());

        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        tabListHandler.onPlayerListPacket(packet);

        assertEquals(1, clientTabList.players.size());
        assertEquals("PlayerB", clientTabList.players.get(0));
        assertEquals((Object) 15, clientTabList.ping.get("PlayerB"));

        tabListHandler.enterContentOperationMode(ContentOperationMode.RECTANGULAR);

        assertEquals(0, clientTabList.players.size());

        tabListHandler.onServerSwitch(false);

        assertEquals(0, clientTabList.players.size());

        tabListHandler.enterContentOperationMode(ContentOperationMode.PASS_TROUGH);

        assertEquals(0, clientTabList.players.size());
    }

    @Test
    public void testRectangular() {
        RectangularTabOverlay.Dimension size = new RectangularTabOverlay.Dimension(3, 9);

        RectangularTabOverlay tabOverlay = tabListHandler.enterContentOperationMode(ContentOperationMode.RECTANGULAR);

        assertEquals(0, clientTabList.players.size());
        assertTrue(tabOverlay.getSupportedSizes().contains(size));

        tabOverlay.setSize(size);

        assertEquals(3 * 9, clientTabList.players.size());
        assertEquals(size, tabOverlay.getSize());

        tabOverlay.setText(2, 1, "0123456789abcdefTEST");
        assertEquals("0123456789abcdef", clientTabList.teams.get(clientTabList.playerToTeamMap.get(clientTabList.players.get(5))).getPrefix());
        assertEquals("TEST", clientTabList.teams.get(clientTabList.playerToTeamMap.get(clientTabList.players.get(5))).getSuffix());
    }

    private static class ClientTabList {
        private Map<String, Integer> ping = new HashMap<>();
        private List<String> players = new ArrayList<>();
        private final Map<String, Team> teams = new HashMap<>();
        private final Map<String, String> playerToTeamMap = new HashMap<>();
    }

    private static class MockLegacyTabList extends AbstractLegacyTabOverlayHandler {
        private final ClientTabList clientTabList;

        public MockLegacyTabList(int maxSize, ClientTabList clientTabList) {
            super(Logger.getLogger("test"), maxSize, Runnable::run, false);
            this.clientTabList = clientTabList;
        }

        @Override
        protected void sendPacket(DefinedPacket packet) {

            if (packet instanceof PlayerListItem) {
                for (PlayerListItem.Item item : ((PlayerListItem) packet).getItems()) {
                    switch (((PlayerListItem) packet).getAction()) {
                        case ADD_PLAYER:
                            String addName = item.getDisplayName() != null ? item.getDisplayName().toPlainText() : item.getUsername();
                            if (!clientTabList.ping.containsKey(addName)) {
                                clientTabList.players.add(addName);
                            }
                            clientTabList.ping.put(addName, item.getPing());
                            break;
                        case REMOVE_PLAYER:
                            String removeName = item.getDisplayName() != null ? item.getDisplayName().toPlainText() : item.getUsername();
                            assertTrue(clientTabList.ping.containsKey(removeName));
                            clientTabList.players.remove(removeName);
                            clientTabList.ping.remove(removeName);
                            break;
                        default:
                            fail();
                    }
                }
            } else if (packet instanceof net.md_5.bungee.protocol.packet.Team) {
                if (((net.md_5.bungee.protocol.packet.Team) packet).getMode() == 1) {
                    Team team = clientTabList.teams.remove(((net.md_5.bungee.protocol.packet.Team) packet).getName());
                    assertNotNull(team);
                    for (String player : team.getPlayers()) {
                        clientTabList.playerToTeamMap.remove(player, team.getName());
                    }

                } else {

                    // Create or get old team
                    net.md_5.bungee.api.score.Team t;
                    if (((net.md_5.bungee.protocol.packet.Team) packet).getMode() == 0) {
                        t = new net.md_5.bungee.api.score.Team(((net.md_5.bungee.protocol.packet.Team) packet).getName());
                        clientTabList.teams.put(((net.md_5.bungee.protocol.packet.Team) packet).getName(), t);
                    } else {
                        t = clientTabList.teams.get(((net.md_5.bungee.protocol.packet.Team) packet).getName());
                    }
                    assertNotNull(t);

                    net.md_5.bungee.protocol.packet.Team tp = (net.md_5.bungee.protocol.packet.Team) packet;
                    if (tp.getMode() == 0 || tp.getMode() == 2) {
                        if (tp.getDisplayName() != null) t.setDisplayName(tp.getDisplayName().getLeft());
                        if (tp.getPrefix() != null) t.setPrefix(tp.getPrefix().getLeft());
                        if (tp.getSuffix() != null) t.setSuffix(tp.getSuffix().getLeft());
                        t.setFriendlyFire(tp.getFriendlyFire());
                        if (tp.getNameTagVisibility() != null) t.setNameTagVisibility(tp.getNameTagVisibility().getLeft());
                        if (tp.getCollisionRule() != null) t.setCollisionRule(tp.getCollisionRule().getLeft());
                        t.setColor(tp.getColor());
                    }
                    if (tp.getPlayers() != null) {
                        for (String s : tp.getPlayers()) {
                            if (tp.getMode() == 0 || tp.getMode() == 3) {
                                if (clientTabList.playerToTeamMap.containsKey(s)) {
                                    clientTabList.teams.get(clientTabList.playerToTeamMap.get(s)).removePlayer(s);
                                }
                                t.addPlayer(s);
                                clientTabList.playerToTeamMap.put(s, t.getName());
                            } else {
                                t.removePlayer(s);
                                assertTrue("Tried to remove player not in team.", clientTabList.playerToTeamMap.remove(s, t.getName()));
                            }
                        }
                    }
                }
            }
        }

        @Override
        public PacketListenerResult onPlayerListPacket(PlayerListItem packet) {
            PacketListenerResult packetListenerResult = super.onPlayerListPacket(packet);
            if (packetListenerResult != PacketListenerResult.CANCEL) {
                sendPacket(packet);
            }
            return packetListenerResult;
        }

        @Override
        public PacketListenerResult onTeamPacket(net.md_5.bungee.protocol.packet.Team packet) {
            PacketListenerResult packetListenerResult = super.onTeamPacket(packet);
            if (packetListenerResult != PacketListenerResult.CANCEL) {
                sendPacket(packet);
            }
            return packetListenerResult;
        }

        @Override
        public PacketListenerResult onPlayerListHeaderFooterPacket(PlayerListHeaderFooter packet) {
            PacketListenerResult packetListenerResult = super.onPlayerListHeaderFooterPacket(packet);
            if (packetListenerResult != PacketListenerResult.CANCEL) {
                sendPacket(packet);
            }
            return packetListenerResult;
        }

        @Override
        public void onServerSwitch(boolean is13OrLater) {
            super.onServerSwitch(is13OrLater);
        }
    }

}