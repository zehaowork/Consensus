import java.util.StringTokenizer;

/**
 * A scanner and parser for requests.
 */

class ReqTokenizer {
    ReqTokenizer() { ; }

    /**
     * Parses requests.
     */
    Token getToken(String req) {
        StringTokenizer sTokenizer = new StringTokenizer(req);
        if (!(sTokenizer.hasMoreTokens()))
            return null;
        String firstToken = sTokenizer.nextToken();
        if (firstToken.equals("JOIN")) {
            if (sTokenizer.hasMoreTokens())

                return new JoinToken(req, sTokenizer.nextToken());
            else
                return null;
        }
        if (firstToken.equals("DETAILS")) {
            while (sTokenizer.hasMoreTokens())
            return new DetailsToken(req, sTokenizer.nextToken(""));
        }
        if (firstToken.equals("VOTE_OPTIONS")) {

            while (sTokenizer.hasMoreTokens())
            return new VoteOptionsToken(req,sTokenizer.nextToken(""));
        }

        if (firstToken.equals("VOTE")) {

            while (sTokenizer.hasMoreTokens())
            return new VoteToken(req, sTokenizer.nextToken(""));
        }

        if (firstToken.equals("EXIT"))
            return new ExitToken(req);


        return null; // Ignore request..


        }
}


/**
 * The Token Prototype.
 */
abstract class Token {
    String _req;
}

/**
 * Syntax: JOIN &lt;name&gt;
 */
class JoinToken extends Token {
    String _name;
    JoinToken(String req, String name) {
        this._req = req;
        this._name = name;
    }
}

/**
 * Syntax: YELL &lt;msg&gt;
 */
class DetailsToken extends Token {
    String portList;

    DetailsToken(String req, String portList) {
        this._req = req;
        this.portList = portList;
    }
}

class VoteOptionsToken extends Token {
    String optionList;

    VoteOptionsToken(String req, String msg) {
        this._req = req;
        this.optionList = msg;
    }
}

class VoteToken extends Token{
    String info;
    VoteToken(String req,String info){
        this._req = req;
        this.info = info;
    }
}

/**
 * Syntax: TELL &lt;rcpt&gt; &lt;msg&gt;
 */


/**
 * Syntax: EXIT
 */
class ExitToken extends Token {
    ExitToken(String req) { this._req = req; }
}