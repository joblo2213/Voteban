package de.voteban.command

import de.voteban.VotebanBot
import de.voteban.db.UserData
import de.voteban.utils.EmbedUtils
import net.dv8tion.jda.core.entities.Message

object WhobannedCommand extends Command("whobanned") {

  override protected def execute(message: Message): Unit = {
    val max = VotebanBot.GUILD_CONFIG(message.getGuild).leaderboard_length
    val sorted = VotebanBot.GUILD_DATA(message.getGuild).users.values.toList.sortWith(sortByBanns)
    val leaders = sorted.slice(0, if (sorted.length < max) sorted.length else max).map(userData => {
      (message.getGuild.getMemberById(userData.userId), userData.bansInitiated)
    })
    message.getChannel.sendMessage(EmbedUtils.whoBannedEmbed(leaders)).queue()
  }

  private def sortByBanns(one: UserData, other: UserData) = {
    one.bansInitiated > other.bansInitiated
  }
}
