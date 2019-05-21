package org.newstextanalyzer;

import java.io.IOException;
import java.util.Date;

public class App {
  // Directory where the search index will be saved
  public final static String TGN_DIRECTORY_PATH = "/Users/gpanez/Documents/news/the_guardian_preprocessed";
  public final static String WORDNET_DIRECTORY_PATH = "/Users/gpanez/Documents/news/wordnet_analysis";

  public static void main(String[] args) throws IOException {
    System.out.println("Start: " + new Date());
    IRAnalysis ira = new IRAnalysis();
    //okok
    //okok
    // ira.buildIndex();
    // ira.deduplicateWordNetWords();
    // ira.explore();

    // new NLPAnalysis().analyze(null);
    // new NLPAnalysis().findInterestingSentences(null, "2018");

    /*
     * TestPipeline tp = new TestPipeline(); BasicPipeline bp = new BasicPipeline();
     * // Extracted from an actual article: //
     * politics/2018/nov/27/theresa-may-heads-to-scotland-as-brexit-deal-founders-in
     * -westminster String text1 =
     * "May heads to Scotland as Brexit deal founders in Westminster.\n" +
     * "Theresa May takes her Brexit publicity blitz to Scotland on Wednesday, as opponents of her deal scramble to plan for the chaotic aftermath of the meaningful House of Commons vote in less than a fortnight, which she appears likely to lose by a crushing margin.\n"
     * +
     * "The prime minister will visit a factory near Glasgow and speak to workers and employers about the agreement, telling them: “It is a deal that is good for Scottish employers and which will protect jobs.”\n"
     * +
     * "Related: 'People were sold a lie': Ruthin restless over May's Brexit deal\n"
     * +
     * "Back in Westminster, few MPs believe the controversial package is likely to pass the Commons, despite a charm offensive from Tory whips, who are pressing the argument that none of the alternatives – from a Canada-style trade deal to a second referendum – could command a majority.\n"
     * +
     * "Jeremy Corbyn’s team is planning for a range of possible scenarios as the Labour leader prepares to ramp up efforts to explain his alternative plan to the public in the coming days. Corbyn is expected to confront the prime minister in a head-to-head television debate.\\n"
     * +
     * "The Labour leadership is determined to reject the idea gaining ground at Westminster of a Norway-plus deal. The Scottish first minister, Nicola Sturgeon, said on Tuesday she hoped a majority could coalesce around the proposal, which is being promoted by the Conservative backbencher Nick Boles.\n"
     * +
     * "Graphic   However, senior Labour party sources insisted they regarded it as an unacceptable abrogation of sovereignty that would fail to honour the referendum result, won by the Vote Leave campaign with the slogan “take back control” – though some shadow cabinet members will be keener to explore it.\n"
     * +
     * "Sturgeon said the alternative framework for the UK, which the Scottish government first advocated in December 2016, “may have met with a disinterested rejection from the UK government but … is gathering support from others. The arguments that the Scottish government has been making are actually winning the day.”\n"
     * +
     * "Almost half of May’s cabinet have held talks to weigh up the possibility of backing the Norway-inspired alternative, in which the UK could join the European Free Trade Association and maintain a customs arrangement with the EU if parliament rejects the prime minister’s EU withdrawal deal next month.\n"
     * +
     * "But Labour intends to hold out for its own plan, involving a permanent customs union and a close relationship with the single market that falls short of full membership. It will resist demands for a second referendum unless all other options have been exhausted.\n"
     * +
     * "Fresh ammunition for MPs hoping to halt Brexit altogether may come from the government’s economic analysis of the deal, which will be published on Wednesday and is expected to echo the findings of a leaked assessment from earlier this year that showed GDP would be hit.\\n"
     * +
     * "Related: Tory divisions: the factions preparing for fall of May's Brexit deal\n"
     * +
     * "The government was keen to stress the report, prepared by civil servants across Whitehall, constituted an “analysis” and not a “forecast”.\n"
     * +
     * "Sturgeon said the Scottish National party would support either the Norway plan or a second referendum, whichever appeared most likely to command a majority in the Commons.\n"
     * +
     * "Leave-leaning Tories are hoping a harder Brexit could emerge if the deal fails to pass the Commons. Several cabinet ministers, including Penny Mordaunt and Andrea Leadsom, have not yet signalled their public support.\n"
     * +
     * "The prime minister’s prospects of winning the vote on 11 December appear increasingly bleak, with 94 of her MPs pledged to vote against her.\n"
     * +
     * "May was forced to defend her plan against hostile comments from the US president, Donald Trump, on Tuesday. He said the agreement “sounds like a great deal for the EU” and warned “they [the UK] may not be able to trade with us”.\n"
     * +
     * "Trump’s intervention came as No 10 attempted to marshal support from MPs in the face of a chorus of condemnation at Westminster. Conservatives were briefed on the deal on Tuesday evening by May’s chief of staff, Gavin Barwell.\n"
     * +
     * "Speaking in Wales on the first leg of a UK-wide tour to promote the deal directly to the British public, the prime minister said: “We will have the ability, outside the European Union, to make those decisions on trade policy for ourselves. It will no longer be a decision being taken by Brussels.\n"
     * +
     * "“As regards the United States, we’ve already been talking to them about the sort of agreement we could have with them in the future.”\n"
     * +
     * "Related: Yes, Donald Trump is talking perfect sense on May’s Brexit deal | Peter Mandelson\n"
     * +
     * "However, Peter Mandelson, a former EU trade commissioner, offered unlikely support for the president’s view, declaring “Donald Trump is right”. The former Labour minister warned in an article for the Guardian that it may not be possible to sign a trade deal with the US until the end of 2022, the last possible date for the end of the post-Brexit transition period, with the UK tied to EU trade rules before that.\n"
     * +
     * "Hard Brexiters who dreamed of the UK becoming a “North Sea Singapore” through deregulation would be disappointed, Mandelson said, because the political declaration signed by May with the EU “agrees that Britain would closely follow EU standards and rules affecting competition”.\n"
     * +
     * "Trade deals, Mandelson concluded, were the product of years of negotiation. “It took Donald Trump to remind the UK government that negotiating trade deals is a long, painful and necessarily complex business,” he wrote. “I can hardly believe I am writing this sentence … Donald Trump is right.”\n"
     * +
     * "Before May arrived in Northern Ireland on the second leg of her tour on Tuesday, the Democratic Unionist party leader, Arlene Foster, renewed her criticism of the Brexit deal, telling the BBC: “The disappointing thing for me is that the prime minister has given up and she is saying … we just have to accept it.”\n"
     * ;
     * 
     * String text2 = "Joe Smith was born in California. " +
     * "In 2017, he went to Paris, France in the summer. " +
     * "His flight left at 3:00pm on July 10th, 2017. " +
     * "After eating some escargot for the first time, Joe said, \"That was delicious!\" "
     * + "He sent a postcard to his sister Jane Smith. " +
     * "After hearing about Joe's trip, Jane decided she might go to France one day."
     * ;
     * 
     * bp.run(text1, "May");
     */

    // VirtuosoTest.run();

    String text3 = "Amber Rudd, who resigned as home secretary on Sunday, "
        + "had only been tasked with the additional role since the former "
        + "education secretary Justine Greening left the government in the January reshuffle.";
//    String text4 = "The apparent hold-up has emerged as sources confirmed that Christine Shawcroft, who resigned on Wednesday night as chair of the disciplinary panel, will no longer sit on the committee overhauling the procedure for dealing with complaints of antisemitism.";
//    text4 = "She also resigns as PPS to John McDonnell and after sustained pressure is suspended from the party a day later.";

    // String text5 = "The Queen’s speech may not go ahead as planned next Monday,
    // No 10 has suggested, which would allow the Conservatives more time to reach a
    // formal agreement with the Democratic Unionist party. The prime minister’s
    // official spokesman declined to confirm that the Queen’s speech would be held
    // on 19 June as previously announced. The leader of the Commons, Andrea
    // Leadsom, would release a statement about the date soon, the spokesman said.
    // But the first secretary of state, Damian Green, confirmed there could be a
    // delay while agreement is sought with the DUP. “Obviously, until we have that
    // we can’t agree the final details of the Queen’s speech,” he said. A
    // government source said the Queen’s speech took a week to be prepared, hinting
    // it could be delayed by a few days. For the speech to be given as scheduled,
    // the contents would need to be decided by Tuesday morning. Also known as the
    // gracious speech, it was historically written on vellum with ink that takes
    // three days to dry. Although it is now written on thick goatskin parchment,
    // this also needs several days to dry, meaning a speech cannot be amended at
    // the last minute. “The lead time is a very long one,” a government source said
    // on Monday. “There is still a political cabinet to come, talks with the DUP,
    // who need to be happy with the contents to ensure they will vote for it, and a
    // full cabinet tomorrow. There is currently no fixed date.” Related: What is a
    // hung parliament and what happens now? The reported delay will raise
    // speculation that Theresa May is not yet certain she can get the legislation
    // passed via a deal with the DUP in Northern Ireland. The prime minister is due
    // to hold talks with the DUP leader, Arlene Foster, on Tuesday morning. “We are
    // working with the DUP on a deal to ensure the safe passage of the Queen’s
    // speech,” May’s spokesman said. Asked again whether the state opening of
    // parliament would go ahead on Monday, the spokesman said: “You can expect the
    // leader of the Commons to issue an update in due course in terms of the state
    // opening. It’s not my job to say any more than I have.” Any delay in the
    // Queen’s speech would be difficult to accommodate, given the monarch is
    // scheduled to attend Royal Ascot from next Tuesday until the end of next week.
    // There has already been one change to the royal diary because of the Queen’s
    // speech: the order of the garter service was cancelled at short notice to
    // accommodate the 19 June date. Arlene Foster: DUP talks with Conservatives
    // ‘positive’ – video The Northern Ireland secretary, James Brokenshire,
    // meanwhile said the Queen’s speech “remains on track”. He told BBC Radio 4’s
    // The World at One: “We are very firmly proceeding on the basis as we have
    // been, on the timeline for the Queen’s speech, on getting it finalised, on
    // making it happen and getting on with the job of running the government.”
    // Downing Street also hinted there may be some flexibility in the start date
    // for Brexit negotiations, which were also due to start next Monday. During the
    // election campaign, May and the Brexit secretary, David Davis, repeatedly said
    // talks would start “just 11 days after the election”. However, Davis told Sky
    // News it was possible the first round of negotiations would not begin next
    // Monday, because of the timing of the Queen’s speech. “It’s in the week of
    // next week, basically, the first discussions,” Davis said, adding that his
    // department’s chief civil servant was in Brussels to sort out the timetable.
    // “It may not be on the Monday, because we’ve also got the Queen’s speech, and
    // I will have to speak in that and so on.” The prime minister’s spokesman told
    // reporters on Monday that Brexit negotiations would “definitely be next week”
    // but said they could not give “a precise date”. “I’m not suggesting there is
    // any confusion,” the prime minister’s spokesman added. “They will start next
    // week. The precise date will be given when we have it.” The spokesman
    // indicated that May intended to remain in office for the whole five-year
    // parliamentary term, telling reporters: “I am not aware of any change in the
    // position she set out in the election campaign.” In an interview on Sunday
    // night, however, May was more ambiguous. “I said during the election campaign
    // that if re-elected I would intend to serve a full term,” she said. “But what
    // I’m doing now is actually getting on with the immediate job. “And I think
    // that’s what’s important, I think that’s what the public would expect. They
    // want to see government providing that certainty and stability at what is a
    // critical time for the country. “The Brexit negotiations start in just a week,
    // we need to get those right and make a success of it. But there are other
    // issues that we need to address in our country. We’ve been listening to
    // voters, and that’s what we will be doing.” A European commission spokesman
    // said it was prepared for the negotiations to start as soon as the UK was
    // ready. “We are fully prepared and ready for negotiations to start,” the
    // spokesman said. “This doesn’t depend entirely on us. We are fully prepared.”
    // Opposition parties seized on the potential delay as another signal the
    // government was failing to function. A Labour source said: “No 10’s failure to
    // confirm the date of the Queen’s speech shows that this government is in chaos
    // as it struggles to agree a backroom deal with a party with abhorrent views on
    // LGBT and women’s rights.” The Liberal Democrat leader, Tim Farron, called it
    // “an utter humiliation for Theresa May” labelling her administration the
    // “May-DUP” government. “This is the biggest embarrassment that a prime
    // minister can face, she cannot announce her agenda because she has no idea
    // what she can actually get through,” he said. The Scottish National party
    // leader, Nicola Sturgeon, also criticised the delay, telling Sky News:
    // “Delaying the Queen’s speech does raise the question: is she really capable
    // of putting together a credible functioning government?” The prime minister
    // chaired a cabinet meeting on Monday, which the Scottish Conservative leader,
    // Ruth Davidson, attended. Later, May was to meet Tory backbench MPs at a
    // meeting of the 1922 Committee in parliament, where she was expected to be
    // questioned about the party’s manifesto and campaign strategy. On Tuesday
    // afternoon, May will travel to Paris for bilateral talks with the French
    // president, Emmanuel Macron, before attending the England v France football
    // match. ";
    //ResignRERegEx rrre = new ResignRERegEx();
    String[] years = new String[] { "2000", "2001", "2002", "2003", "2004", "2005", "2006", "2007", "2008", "2009",
        "2010", "2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018" };
    // new NLPAnalysis().findInterestingSentences(rrre, new String[] {"2018"});

    new NLPAnalysis().findInterestingSentences(
        new Pipeline(new IPipelineStep[] { new SimpleClassifier(), new ReVerbWrapper(), new EntityValidator(), new VirtuosoPersistor() }), new String[] { "2018" });
    // rre.run(text3);
//    rre.run(text4);
    // srre.run(text5);

    // String text5 = "Jeremy Heywood resigned as UK Cabinet Secretary due of ill
    // health following a three-month leave of absence. (October 24)";
//    ResignREBoot rrb = new ResignREBoot();
    // rrb.run(text5);
//    new NLPAnalysis().findInterestingSentences(rrb, null);
    System.out.println("End: " + new Date());
    System.out.println("done");
  }
}