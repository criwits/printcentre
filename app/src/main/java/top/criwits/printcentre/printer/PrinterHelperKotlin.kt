package top.criwits.printcentre.printer

import de.gmuth.ipp.client.IppJob
import de.gmuth.ipp.client.IppPrinter
import de.gmuth.ipp.client.IppTemplateAttributes.copies
import de.gmuth.ipp.client.IppTemplateAttributes.documentFormat
import de.gmuth.ipp.client.IppTemplateAttributes.jobName
import de.gmuth.ipp.client.IppTemplateAttributes.jobPriority
import de.gmuth.ipp.client.IppTemplateAttributes.printerResolution
import java.io.File

object PrinterHelperKotlin {
    fun printJob(printer: IppPrinter, file: File, copies: Int): IppJob {
        val job = printer.printJob(
            file,
            jobName(file.name),
            jobPriority(30),
            documentFormat("image/urf"),
            copies(copies),
            printerResolution(600),
            notifyEvents = listOf("job-state-changed", "job-stopped", "job-completed") // CUPS
        )
        return job
    }
}