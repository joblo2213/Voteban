package de.voteban.config

import java.io._
import java.nio.charset.StandardCharsets

import de.voteban.config.XMLConfigurationService.{CONFIG_DIR, DEFAULT_CONFIG, GUILD_CONFIG_FILE_REGEX, guildConfigFile}
import de.voteban.utils.WithLogger

import scala.collection.concurrent.TrieMap
import scala.util.matching.Regex
import scala.xml.{Node, PrettyPrinter}

/**
  * Service for loading and saving configuration files.
  */
class XMLConfigurationService extends WithLogger {
  //TODO Better Caching

  private val cachedConfigs = TrieMap[Long, GuildConfig]()

  /**
    * Load all configurazion files into cache
    */
  def loadCache(): Unit = {
    Option(CONFIG_DIR.listFiles()).foreach(_.foreach(f =>
      f.getName match {
        case GUILD_CONFIG_FILE_REGEX(guildId) => loadGuildConfig(guildId.toLong)
        case _=> //Ignore default
      }
      ))
  }

  /**
    * Tries to load the configuration for a guild from a xml file
    *
    * Uses cached configs if available
    *
    * @param guildId the id of the guild that should be loaded
    * @return config of that guild
    * @throws Exception if config loading failed
    */
  def loadGuildConfig(guildId: Long): GuildConfig = {
    cachedConfigs.getOrElse(guildId, {
      val file = guildConfigFile(guildId)
      val cfg = if (!file.exists()) {
        DEFAULT_CONFIG(guildId)
      } else {
        this synchronized {
          readGuildConfig(new FileInputStream(file))
        }
      }
      cachedConfigs += (guildId -> cfg)
      cfg
    })
  }

  /**
    * Tries to read the xml configuration for a guild from an input stream
    *
    * Doesn't cache the loaded configuration
    *
    * @param inputStream the reader providing the xml config file input
    * @return the parsed config object
    * @throws Exception if config loading failed
    */
  def readGuildConfig(inputStream: InputStream): GuildConfig = {
    val config = xml.Utility.trim(xml.XML.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) \ "config"
    GuildConfig(
      (config \ "guildId").text.toLong
      //Read other config values from config
    )
  }


  /**
    * Tries to save the configuration for a guild to a xml file
    *
    * @param guildConfig the configuration object to save
    * @throws Exception if config saving failed
    */
  def saveGuildConfig(guildConfig: GuildConfig): Unit = {
    val file = guildConfigFile(guildConfig.guildId)
    this synchronized {
      writeGuildConfig(guildConfig, new FileOutputStream(file, false))
    }
  }

  /**
    * Tries to write the configuration for a guild to an output stream
    *
    * @param guildConfig
    * @param outputStream
    */
  def writeGuildConfig(guildConfig: GuildConfig, outputStream: OutputStream): Unit = {
    val writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))
    val config: Node =
      <config>
        <guildId>
          {guildConfig.guildId}
        </guildId>
      </config>
    //Save config values to config
    writer.print(new PrettyPrinter(120, 2).format(config))
    writer.close()
  }

}

object XMLConfigurationService {

  // language=RegExp
  val GUILD_CONFIG_FILE_REGEX: Regex = "guild-(\\d+).xml".r

  val CONFIG_DIR = new File("config")

  def DEFAULT_CONFIG(guildId: Long): GuildConfig = GuildConfig(
    guildId
    //Add default values
  )

  def guildConfigFile(guildId: Long): File = new File(CONFIG_DIR, s"guild-$guildId.xml")
}