package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SalesTrack", appName)
  }

  @Test
  fun `verify consignment sales calculation`() {
    val previousStock = 10
    val remainingStock = 3
    val sold = previousStock - remainingStock
    val sellPrice = 15000.0
    val costPrice = 11000.0

    val subtotal = sold * sellPrice
    val profit = sold * (sellPrice - costPrice)

    assertEquals(7, sold)
    assertEquals(105000.0, subtotal, 0.001)
    assertEquals(28000.0, profit, 0.001)
  }
}
