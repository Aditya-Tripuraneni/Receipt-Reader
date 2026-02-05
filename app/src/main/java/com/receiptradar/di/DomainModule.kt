package com.receiptradar.di

import android.content.Context
import com.receiptradar.domain.pipeline.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for domain/pipeline dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    
    @Provides
    @Singleton
    fun provideImagePreprocessor(
        @ApplicationContext context: Context
    ): ImagePreprocessor {
        return ImagePreprocessor(context)
    }
    
    @Provides
    @Singleton
    fun provideCurrencyDetector(): CurrencyDetector {
        return CurrencyDetector()
    }
    
    @Provides
    @Singleton
    fun provideOcrEngine(): OcrEngine {
        return OcrEngine()
    }
    
    @Provides
    @Singleton
    fun provideLineParser(): LineParser {
        return LineParser()
    }
    
    @Provides
    @Singleton
    fun providePriceExtractor(): PriceExtractor {
        return PriceExtractor()
    }
    
    @Provides
    @Singleton
    fun provideDateExtractor(): DateExtractor {
        return DateExtractor()
    }
    
    @Provides
    @Singleton
    fun provideMerchantExtractor(): MerchantExtractor {
        return MerchantExtractor()
    }
    
    @Provides
    @Singleton
    fun provideCategorizer(): Categorizer {
        return Categorizer()
    }
    
    @Provides
    @Singleton
    fun provideReceiptProcessor(
        imagePreprocessor: ImagePreprocessor,
        ocrEngine: OcrEngine,
        lineParser: LineParser,
        priceExtractor: PriceExtractor,
        dateExtractor: DateExtractor,
        merchantExtractor: MerchantExtractor,
        categorizer: Categorizer,
        currencyDetector: CurrencyDetector
    ): ReceiptProcessor {
        return ReceiptProcessor(
            imagePreprocessor,
            ocrEngine,
            lineParser,
            priceExtractor,
            dateExtractor,
            merchantExtractor,
            categorizer,
            currencyDetector
        )
    }
}
