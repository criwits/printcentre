package top.criwits.printcentre.printer

import de.gmuth.ipp.client.IppColorMode
import de.gmuth.ipp.client.IppFinishing
import de.gmuth.ipp.client.IppJob
import de.gmuth.ipp.client.IppPrintQuality
import de.gmuth.ipp.client.IppPrinter
import de.gmuth.ipp.client.IppSides
import de.gmuth.ipp.client.IppTemplateAttributes.copies
import de.gmuth.ipp.client.IppTemplateAttributes.documentFormat
import de.gmuth.ipp.client.IppTemplateAttributes.finishings
import de.gmuth.ipp.client.IppTemplateAttributes.jobName
import de.gmuth.ipp.client.IppTemplateAttributes.jobPriority
import de.gmuth.ipp.client.IppTemplateAttributes.media
import de.gmuth.ipp.client.IppTemplateAttributes.mediaColSource
import de.gmuth.ipp.client.IppTemplateAttributes.numberUp
import de.gmuth.ipp.client.IppTemplateAttributes.printerResolution
import java.io.File

object PrinterControllerKotlinUtils {
    fun printJob(printer: IppPrinter, file: File, copies: Int): IppJob {
        val job =  printer.createJob(
            jobName(file.name),
        )
        job.sendDocument(file)
        return job
    }
}