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
        val probationHybridBN = createMockUser(OsuRole.OBS, 4236057) // 4236057 = Sinnoh

        assertEquals(OsuRole.OBS, getUserRole(observer, emptyList()))
        assertEquals(OsuRole.OBS, getUserRole(observer, listOf(GroupBadge(4))))

        assertEquals(OsuRole.NAT, getUserRole(observer, listOf(GroupBadge(7))))
        assertEquals(OsuRole.NAT, getUserRole(observer, listOf(GroupBadge(4), GroupBadge(7)))) // Deif GMT & NAT

        assertEquals(OsuRole.BN, getUserRole(observer, listOf(GroupBadge(28))))
        assertEquals(OsuRole.BN, getUserRole(observer, listOf(GroupBadge(4), GroupBadge(28)))) // JBH GMT & BN

        assertEquals(OsuRole.PBN, getUserRole(observer, listOf(GroupBadge(32))))
        assertEquals(OsuRole.PBN, getUserRole(observer, listOf(GroupBadge(4), GroupBadge(32))))

        assertEquals(OsuRole.CA, getUserRole(catchAlumni, emptyList()))
        assertEquals(OsuRole.CA, getUserRole(catchNATAlumni, emptyList()))
        assertEquals(OsuRole.CA, getUserRole(catchBNAlumni, emptyList()))
        assertEquals(OsuRole.CA, getUserRole(catchPBNAlumni, emptyList()))

        assertEquals(OsuRole.PBN, getUserRole(probationHybridBN, listOf(GroupBadge(28))))
    }

    private fun createMockUser(role: OsuRole, osuId: Long = 0) = User(osuId, "", "", role = role)
}