class ListCommandsParser:

    def get_parsed_data(self, input, delimiter=' '):
        data = []
        lines = input.strip().split("\n")

        if ((len(lines) > 0) and ((lines[0].upper().find("ERROR") > 0) or
                                  (lines[0].upper().find("NOT FOUND") > 0) or
                                  (len(lines) == 1
                                   and lines[0].strip() == ""))):
            return data

        for line in lines:
            if line.strip() != "":
                data.append(line.strip())

        data = data[:-1]

        pydicts = []
        for line in data:
            pydict = dict()
            pydict.update({'command': line.strip()})
            pydicts.append(pydict)

        parsed_data = {'table': pydicts}

        return parsed_data
