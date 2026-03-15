package io.github.zapolyarnydev.ptktimetable.data.normalize

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LessonTextNormalizerTest {

    private val normalizer = LessonTextNormalizer()

    @Test
    fun `normalize extracts subject teacher and classroom`() {
        val normalized = normalizer.normalize(
            "CMS WordPress, Ильин В.Р., ауд.403"
        )

        assertEquals("CMS WordPress", normalized.subject)
        assertEquals("Ильин В.Р.", normalized.teacher)
        assertEquals("ауд.403", normalized.classroom)
    }

    @Test
    fun `normalize keeps subgroup marker in subject and extracts teacher classroom`() {
        val normalized = normalizer.normalize(
            "Иностранный язык в профессиональной деятельности, п/г 1, Пименова Т.М., ауд.303"
        )

        assertEquals(
            "Иностранный язык в профессиональной деятельности, п/г 1",
            normalized.subject
        )
        assertEquals("Пименова Т.М.", normalized.teacher)
        assertEquals("ауд.303", normalized.classroom)
    }

    @Test
    fun `normalize keeps only subject when teacher and classroom are absent`() {
        val normalized = normalizer.normalize("проектный практикум")

        assertEquals("проектный практикум", normalized.subject)
        assertNull(normalized.teacher)
        assertNull(normalized.classroom)
    }
}
