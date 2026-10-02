package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SubscriptionType
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldPrimaryLight
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.NoteCorrectGreen
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardStroke
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AvanViewModel
import com.example.util.PersianUtils

@Composable
fun SubscriptionScreen(
    viewModel: AvanViewModel
) {
    val selectedPlan by viewModel.selectedSubscription.collectAsState()
    val isPaymentOpen by viewModel.isPaymentDialogOpen.collectAsState()
    val paymentState by viewModel.paymentState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDeep)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x33F59E0B))
                .border(1.dp, GoldPrimary, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = GoldPrimaryLight,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "طرح‌های اشتراک اختصاصی آوان",
                    color = GoldPrimaryLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "آموزش حرفه‌ای و نامحدود پیانو",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "با اشتراک ویژه، تمام قطعات فاخر، تحلیل‌های عمیق هوش مصنوعی و مسیر آموزشی شخصی‌سازی شده را فعال کنید.",
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Annual Plan Card (Recommended)
        SubscriptionPlanCard(
            plan = SubscriptionType.ANNUAL,
            priceLabel = "۴٬۵۰۰٬۰۰۰ تومان",
            subPriceLabel = "معادل ماهانه: ۳۷۵٬۰۰۰ تومان",
            discountBadge = "۳۷.۵٪ ارزان‌تر از پرداخت ماهانه در یک سال",
            isSelected = selectedPlan == SubscriptionType.ANNUAL,
            isBestValue = true,
            onSelect = { viewModel.selectSubscriptionPlan(SubscriptionType.ANNUAL) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Monthly Plan Card
        SubscriptionPlanCard(
            plan = SubscriptionType.MONTHLY,
            priceLabel = "۶۰۰٬۰۰۰ تومان",
            subPriceLabel = "پرداخت دوره‌ای هر ماه",
            discountBadge = null,
            isSelected = selectedPlan == SubscriptionType.MONTHLY,
            isBestValue = false,
            onSelect = { viewModel.selectSubscriptionPlan(SubscriptionType.MONTHLY) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Premium Features Checklist
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(StudioCardBg)
                .border(1.dp, StudioCardStroke, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "آنچه در اشتراک ویژه دریافت می‌کنید:",
                    color = GoldPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                PremiumFeatureRow(text = "معلم خصوصی هوشمند بدون محدودیت و در لحظه")
                PremiumFeatureRow(text = "تحلیل کامل و چندبعدی عملکرد نواختن پیانو")
                PremiumFeatureRow(text = "مسیر یادگیری کاملاً پویا و اختصاصی شما")
                PremiumFeatureRow(text = "تحلیل پیشرفته‌ی خطاها و پیشنهاد تمرینات هدفمند")
                PremiumFeatureRow(text = "آموزش تعاملی تمام قطعات ایرانی و کلاسیک")
                PremiumFeatureRow(text = "نمودارهای تحلیلی دقیق پیشرفت و توصیه کتب مرجع")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.openSubscriptionDialog(selectedPlan) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("checkout_button"),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = ObsidianDeep,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "اتصال به درگاه بانکی شاپرک و فعال‌سازی",
                    color = ObsidianDeep,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }

    // Iranian Online Payment Gateway Dialog (Shaparak Flow)
    if (isPaymentOpen) {
        IranianPaymentGatewayDialog(
            plan = selectedPlan,
            paymentState = paymentState,
            onDismiss = { viewModel.closePaymentDialog() },
            onPay = { viewModel.processPayment() }
        )
    }
}

@Composable
private fun SubscriptionPlanCard(
    plan: SubscriptionType,
    priceLabel: String,
    subPriceLabel: String,
    discountBadge: String?,
    isSelected: Boolean,
    isBestValue: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2B2113), MidnightDark)
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(StudioCardBg, StudioCardBg)
                    )
                }
            )
            .border(
                2.dp,
                if (isSelected) GoldPrimary else StudioCardStroke,
                RoundedCornerShape(16.dp)
            )
            .clickable { onSelect() }
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .border(2.dp, if (isSelected) GoldPrimary else TextSecondary, CircleShape)
                            .background(if (isSelected) GoldPrimary else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(ObsidianDeep)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = plan.title,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isBestValue) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldPrimary)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "پیشنهاد ویژه",
                            color = ObsidianDeep,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = priceLabel,
                        color = GoldPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subPriceLabel,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            if (discountBadge != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x3310B981))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = discountBadge,
                        color = NoteCorrectGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumFeatureRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(Color(0x3310B981)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = NoteCorrectGreen,
                modifier = Modifier.size(12.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            color = TextPrimary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun IranianPaymentGatewayDialog(
    plan: SubscriptionType,
    paymentState: String,
    onDismiss: () -> Unit,
    onPay: () -> Unit
) {
    var cardNumber by remember { mutableStateOf("۶۰۳۷-۹۹۱۹-") }
    var cvv2 by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (paymentState != "PROCESSING") onDismiss() },
        containerColor = StudioCardBg,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "درگاه پرداخت امن شاپرک",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "بستن", tint = TextSecondary)
                }
            }
        },
        text = {
            Column {
                if (paymentState == "SUCCESS") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(NoteCorrectGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = ObsidianDeep, modifier = Modifier.size(36.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "پرداخت با موفقیت انجام شد!",
                            color = NoteCorrectGreen,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "اشتراک طلایی آوان برای شما فعال شد و تمام قابلیت‌های هوش مصنوعی بازگشایی شدند.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Text(
                        text = "مبلغ قابل پرداخت: ${PersianUtils.formatPriceToman(plan.priceToman)}",
                        color = GoldPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        label = { Text("شماره کارت بانکی", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = StudioCardStroke
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = cvv2,
                            onValueChange = { cvv2 = it },
                            label = { Text("CVV2", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = StudioCardStroke
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { otpCode = it },
                            label = { Text("رمز پویا", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = StudioCardStroke
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (paymentState == "SUCCESS") {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = NoteCorrectGreen)
                ) {
                    Text(text = "شروع یادگیری با اشتراک ویژه", color = ObsidianDeep, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onPay,
                    enabled = paymentState != "PROCESSING",
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (paymentState == "PROCESSING") {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ObsidianDeep, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "در حال اتصال به بانک...", color = ObsidianDeep)
                    } else {
                        Text(
                            text = "پرداخت و تایید نهایی",
                            color = ObsidianDeep,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    )
}
