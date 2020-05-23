package nl.greaper.bnplanner.util

import nl.greaper.bnplanner.model.osu.GroupBadge
import nl.greaper.bnplanner.model.user.OsuRole
import nl.greaper.bnplanner.model.user.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OsuUtilTests {

    @Test
    fun testGetUserRole() {
        val observer = createMockUser(OsuRole.OBS)
        val catchAlumni = createMockUser(OsuRole.CA)
        val catchNATAlumni = createMockUser(OsuRole.NAT)
        val catchBNAlumni = createMockUser(OsuRole.BN)
        val catchPBNAlumni = createMockUser(OsuRole.PBN)
        val gmtNat = createMockUser(OsuRole.OBS, 318565) // 318565 = Deif
        val probationHybridBN = createMockUser(OsuRole.OBS, 4236057) // 4236057 = Sinnoh

        assertEquals(OsuRole.OBS, getUserRole(observer, null))
        assertEquals(OsuRole.OBS, getUserRole(observer, GroupBadge(4)))

        assertEquals(OsuRole.NAT, getUserRole(observer, GroupBadge(7)))
        assertEquals(OsuRole.BN, getUserRole(observer, GroupBadge(28)))
        assertEquals(OsuRole.PBN, getUserRole(observer, GroupBadge(32)))

        assertEquals(OsuRole.CA, getUserRole(catchAlumni, null))
        assertEquals(OsuRole.CA, getUserRole(catchNATAlumni, null))
        assertEquals(OsuRole.CA, getUserRole(catchBNAlumni, null))
        assertEquals(OsuRole.CA, getUserRole(catchPBNAlumni, null))

        assertEquals(OsuRole.NAT, getUserRole(gmtNat, GroupBadge(4)))
        assertEquals(OsuRole.PBN, getUserRole(probationHybridBN, GroupBadge(28)))
    }

    private fun createMockUser(role: OsuRole, osuId: Long = 0) = User(osuId, "", "", role = role)
}