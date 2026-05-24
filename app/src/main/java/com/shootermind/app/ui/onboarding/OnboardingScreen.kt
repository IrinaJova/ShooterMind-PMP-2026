package com.shootermind.app.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shootermind.app.R
import com.shootermind.app.core.util.LocaleUtils
import com.shootermind.app.core.util.OnboardingPrefs
import kotlinx.coroutines.launch

private val PurpleDark  = Color(0xFF5B21B6)
private val PurpleLight = Color(0xFF8B5CF6)
private val Amber       = Color(0xFFF59E0B)
private val White       = Color.White
private val WhiteTrans  = Color(0x40FFFFFF)

private data class OnboardingSlide(
    val emoji: String,
    val titleRes: Int,
    val bodyRes: Int
)

private val slides = listOf(
    OnboardingSlide("🎯", R.string.onboarding_slide1_title, R.string.onboarding_slide1_body),
    OnboardingSlide("📊", R.string.onboarding_slide2_title, R.string.onboarding_slide2_body),
    OnboardingSlide("📈", R.string.onboarding_slide3_title, R.string.onboarding_slide3_body),
    OnboardingSlide("📝", R.string.onboarding_slide4_title, R.string.onboarding_slide4_body),
    OnboardingSlide("🏆", R.string.onboarding_slide5_title, R.string.onboarding_slide5_body),
)

/**
 * Page 0 = language picker
 * Pages 1–5 = welcome slides
 */
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val context   = LocalContext.current
    val scope     = rememberCoroutineScope()
    val totalPages = 6 // 0 = lang, 1-5 = slides
    val pagerState = rememberPagerState(pageCount = { totalPages })

    // Default to "en", updated by language page
    var selectedLang by remember {
        mutableStateOf(OnboardingPrefs.getSelectedLanguage(context))
    }

    val gradient = Brush.verticalGradient(listOf(PurpleLight, PurpleDark))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Skip button (top right, hidden on last page) ────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, end = 16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                if (pagerState.currentPage < totalPages - 1) {
                    TextButton(
                        onClick = {
                            OnboardingPrefs.markCompleted(context)
                            OnboardingPrefs.setLanguage(context, selectedLang)
                            LocaleUtils.setLocale(selectedLang)
                            onComplete()
                        }
                    ) {
                        Text(
                            text  = stringResource(R.string.onboarding_skip),
                            color = White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // ── Pager content ───────────────────────────────────────────────
            HorizontalPager(
                state    = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                if (page == 0) {
                    LanguagePage(
                        selectedLang = selectedLang,
                        onLanguageSelected = { lang ->
                            selectedLang = lang
                            OnboardingPrefs.setLanguage(context, lang)
                        }
                    )
                } else {
                    SlidePage(slide = slides[page - 1])
                }
            }

            // ── Dots ────────────────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                repeat(totalPages) { idx ->
                    val isActive   = idx == pagerState.currentPage
                    val dotWidthF  by animateFloatAsState(
                        targetValue = if (isActive) 24f else 8f, label = "dotWidth"
                    )
                    val dotColor   by animateColorAsState(
                        targetValue = if (isActive) Amber else WhiteTrans, label = "dotColor"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(8.dp)
                            .width(dotWidthF.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }

            // ── Navigation buttons ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                if (pagerState.currentPage > 0) {
                    OutlinedButton(
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        },
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = White),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, WhiteTrans)
                    ) {
                        Text(stringResource(R.string.onboarding_back))
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                }

                if (pagerState.currentPage < totalPages - 1) {
                    Button(
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        },
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.buttonColors(containerColor = Amber)
                    ) {
                        Text(
                            text  = stringResource(R.string.onboarding_next),
                            color = Color(0xFF1C0F00),
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            OnboardingPrefs.markCompleted(context)
                            OnboardingPrefs.setLanguage(context, selectedLang)
                            LocaleUtils.setLocale(selectedLang)
                            onComplete()
                        },
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.buttonColors(containerColor = Amber)
                    ) {
                        Text(
                            text  = stringResource(R.string.onboarding_get_started),
                            color = Color(0xFF1C0F00),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ── Language picker page ────────────────────────────────────────────────────

@Composable
private fun LanguagePage(
    selectedLang      : String,
    onLanguageSelected: (String) -> Unit
) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text       = "🌍",
            fontSize   = 64.sp,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text       = stringResource(R.string.onboarding_choose_language),
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color      = White,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = stringResource(R.string.onboarding_lang_subtitle),
            style     = MaterialTheme.typography.bodyMedium,
            color     = White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(48.dp))

        LanguageChip(
            label    = "English",
            tag      = "en",
            selected = selectedLang == "en",
            onClick  = { onLanguageSelected("en") }
        )
        Spacer(Modifier.height(16.dp))
        LanguageChip(
            label    = "Македонски",
            tag      = "mk",
            selected = selectedLang == "mk",
            onClick  = { onLanguageSelected("mk") }
        )
    }
}

@Composable
private fun LanguageChip(
    label   : String,
    tag     : String,
    selected: Boolean,
    onClick : () -> Unit
) {
    val bg     = if (selected) Amber           else WhiteTrans
    val border = if (selected) Amber           else White.copy(alpha = 0.5f)
    val text   = if (selected) Color(0xFF1C0F00) else White

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(2.dp, border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color      = text
        )
    }
}

// ── Welcome slide page ──────────────────────────────────────────────────────

@Composable
private fun SlidePage(slide: OnboardingSlide) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text      = slide.emoji,
            fontSize  = 80.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Text(
            text       = stringResource(slide.titleRes),
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color      = White,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text      = stringResource(slide.bodyRes),
            style     = MaterialTheme.typography.bodyLarge,
            color     = White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )
    }
}
